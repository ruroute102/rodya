#!/usr/bin/env python3
"""Generate an app-ready Audi Q3 8U service GLB.

The model is intentionally procedural so it can live in the repository and be
iterated without external DCC dependencies. It is not a CAD-perfect vehicle; it
is a named-node GLB scaffold with Audi Q3 proportions and service-focused fuel
pump internals for the Android app.
"""

from __future__ import annotations

import json
import math
import struct
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable


ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "android/app/src/main/assets/models/audi/q3/8u/2011_2_0_tfsi"
OUT_GLB = OUT_DIR / "model.glb"
OUT_PREVIEW = OUT_DIR / "preview.png"


@dataclass(frozen=True)
class V3:
    x: float
    y: float
    z: float

    def __add__(self, other: "V3") -> "V3":
        return V3(self.x + other.x, self.y + other.y, self.z + other.z)

    def __sub__(self, other: "V3") -> "V3":
        return V3(self.x - other.x, self.y - other.y, self.z - other.z)

    def __mul__(self, value: float) -> "V3":
        return V3(self.x * value, self.y * value, self.z * value)

    def as_tuple(self) -> tuple[float, float, float]:
        return (self.x, self.y, self.z)


def cross(a: V3, b: V3) -> V3:
    return V3(
        a.y * b.z - a.z * b.y,
        a.z * b.x - a.x * b.z,
        a.x * b.y - a.y * b.x,
    )


def dot(a: V3, b: V3) -> float:
    return a.x * b.x + a.y * b.y + a.z * b.z


def length(v: V3) -> float:
    return math.sqrt(dot(v, v))


def normalized(v: V3) -> V3:
    l = length(v)
    if l < 1e-8:
        return V3(0.0, 1.0, 0.0)
    return V3(v.x / l, v.y / l, v.z / l)


def lerp(a: V3, b: V3, t: float) -> V3:
    return a * (1.0 - t) + b * t


class Mesh:
    def __init__(self, name: str, material: str) -> None:
        self.name = name
        self.material = material
        self.positions: list[V3] = []
        self.normals: list[V3] = []
        self.indices: list[int] = []

    def tri(self, a: V3, b: V3, c: V3) -> None:
        n = normalized(cross(b - a, c - a))
        base = len(self.positions)
        self.positions.extend([a, b, c])
        self.normals.extend([n, n, n])
        self.indices.extend([base, base + 1, base + 2])

    def quad(self, a: V3, b: V3, c: V3, d: V3) -> None:
        self.tri(a, b, c)
        self.tri(a, c, d)

    def add_box(self, center: V3, size: V3) -> None:
        hx, hy, hz = size.x / 2, size.y / 2, size.z / 2
        x0, x1 = center.x - hx, center.x + hx
        y0, y1 = center.y - hy, center.y + hy
        z0, z1 = center.z - hz, center.z + hz
        p000 = V3(x0, y0, z0)
        p100 = V3(x1, y0, z0)
        p110 = V3(x1, y1, z0)
        p010 = V3(x0, y1, z0)
        p001 = V3(x0, y0, z1)
        p101 = V3(x1, y0, z1)
        p111 = V3(x1, y1, z1)
        p011 = V3(x0, y1, z1)
        self.quad(p010, p110, p111, p011)  # top
        self.quad(p000, p001, p101, p100)  # bottom
        self.quad(p001, p011, p111, p101)  # front
        self.quad(p100, p110, p010, p000)  # rear
        self.quad(p101, p111, p110, p100)  # right
        self.quad(p000, p010, p011, p001)  # left

    def add_loft(self, rings: list[list[V3]], cap_start: bool = True, cap_end: bool = True) -> None:
        n = len(rings[0])
        for ring in rings:
            if len(ring) != n:
                raise ValueError("all rings must have the same point count")
        for s in range(len(rings) - 1):
            r0, r1 = rings[s], rings[s + 1]
            for i in range(n):
                self.quad(r0[i], r0[(i + 1) % n], r1[(i + 1) % n], r1[i])
        if cap_start:
            self._cap_ring(rings[0], reverse=True)
        if cap_end:
            self._cap_ring(rings[-1], reverse=False)

    def _cap_ring(self, ring: list[V3], reverse: bool) -> None:
        c = V3(
            sum(p.x for p in ring) / len(ring),
            sum(p.y for p in ring) / len(ring),
            sum(p.z for p in ring) / len(ring),
        )
        for i in range(len(ring)):
            if reverse:
                self.tri(c, ring[(i + 1) % len(ring)], ring[i])
            else:
                self.tri(c, ring[i], ring[(i + 1) % len(ring)])

    def add_cylinder_between(
        self,
        start: V3,
        end: V3,
        radius: float,
        segments: int = 24,
        cap: bool = True,
    ) -> None:
        axis = normalized(end - start)
        helper = V3(0.0, 1.0, 0.0) if abs(axis.y) < 0.92 else V3(1.0, 0.0, 0.0)
        u = normalized(cross(axis, helper))
        v = normalized(cross(axis, u))
        r0: list[V3] = []
        r1: list[V3] = []
        for i in range(segments):
            a = 2.0 * math.pi * i / segments
            off = u * (math.cos(a) * radius) + v * (math.sin(a) * radius)
            r0.append(start + off)
            r1.append(end + off)
        self.add_loft([r0, r1], cap_start=cap, cap_end=cap)

    def add_cylinder_y(self, center: V3, radius: float, height: float, segments: int = 32) -> None:
        self.add_cylinder_between(
            V3(center.x, center.y - height / 2, center.z),
            V3(center.x, center.y + height / 2, center.z),
            radius,
            segments,
        )

    def add_ellipsoid(self, center: V3, radii: V3, segments: int = 32, rings: int = 14) -> None:
        bands: list[list[V3]] = []
        for j in range(rings + 1):
            v = -math.pi / 2 + math.pi * j / rings
            cy = math.sin(v)
            rr = math.cos(v)
            ring: list[V3] = []
            for i in range(segments):
                u = 2.0 * math.pi * i / segments
                ring.append(
                    V3(
                        center.x + radii.x * math.cos(u) * rr,
                        center.y + radii.y * cy,
                        center.z + radii.z * math.sin(u) * rr,
                    )
                )
            bands.append(ring)
        self.add_loft(bands, cap_start=False, cap_end=False)

    def add_torus(
        self,
        center: V3,
        major: float,
        minor: float,
        axis: str,
        seg_major: int = 40,
        seg_minor: int = 10,
    ) -> None:
        rings: list[list[V3]] = []
        for i in range(seg_major):
            a = 2.0 * math.pi * i / seg_major
            ring: list[V3] = []
            for j in range(seg_minor):
                b = 2.0 * math.pi * j / seg_minor
                r = major + minor * math.cos(b)
                m = minor * math.sin(b)
                if axis == "x":
                    p = V3(center.x + m, center.y + r * math.cos(a), center.z + r * math.sin(a))
                elif axis == "y":
                    p = V3(center.x + r * math.cos(a), center.y + m, center.z + r * math.sin(a))
                else:
                    p = V3(center.x + r * math.cos(a), center.y + r * math.sin(a), center.z + m)
                ring.append(p)
            rings.append(ring)
        self.add_loft(rings, cap_start=False, cap_end=False)


def body_profile(
    z: float,
    floor_y: float,
    sill_w: float,
    lower_w: float,
    belt_y: float,
    belt_w: float,
    shoulder_y: float,
    shoulder_w: float,
    roof_y: float,
    roof_w: float,
) -> list[V3]:
    return [
        V3(-sill_w, floor_y, z),
        V3(sill_w, floor_y, z),
        V3(lower_w, floor_y + 0.17, z),
        V3(belt_w, belt_y, z),
        V3(shoulder_w, shoulder_y, z),
        V3(roof_w, roof_y, z),
        V3(0.0, roof_y + 0.035, z),
        V3(-roof_w, roof_y, z),
        V3(-shoulder_w, shoulder_y, z),
        V3(-belt_w, belt_y, z),
        V3(-lower_w, floor_y + 0.17, z),
    ]


def dense_profiles(profiles: list[list[V3]], between: int = 1) -> list[list[V3]]:
    if between <= 0:
        return profiles
    result: list[list[V3]] = []
    for i in range(len(profiles) - 1):
        result.append(profiles[i])
        for k in range(1, between + 1):
            t = k / (between + 1)
            t = t * t * (3 - 2 * t)
            result.append([lerp(a, b, t) for a, b in zip(profiles[i], profiles[i + 1])])
    result.append(profiles[-1])
    return result


def make_wheel(name: str, x: float, z: float) -> Mesh:
    m = Mesh(name, "rubber")
    center = V3(x, 0.34, z)
    m.add_torus(center, major=0.285, minor=0.085, axis="x", seg_major=48, seg_minor=12)
    m.add_cylinder_between(V3(x - 0.13, 0.34, z), V3(x + 0.13, 0.34, z), 0.20, 40)
    for i in range(10):
        a = 2 * math.pi * i / 10
        y = center.y + math.cos(a) * 0.105
        zz = center.z + math.sin(a) * 0.105
        yy = center.y + math.cos(a) * 0.205
        zzz = center.z + math.sin(a) * 0.205
        spoke = Mesh(name + "_spoke", "rim")
        spoke.add_cylinder_between(V3(x + 0.135, y, zz), V3(x + 0.145, yy, zzz), 0.012, 8)
        append_mesh(m, spoke)
    return m


def append_mesh(target: Mesh, src: Mesh) -> None:
    offset = len(target.positions)
    target.positions.extend(src.positions)
    target.normals.extend(src.normals)
    target.indices.extend([idx + offset for idx in src.indices])


def build_model_meshes() -> list[Mesh]:
    meshes: list[Mesh] = []

    # Exterior body: Audi Q3 8U proportions, length 4.385m, width 1.831m,
    # height 1.59m without roof antenna.
    body = Mesh("body_shell", "xray_shell")
    raw_profiles = [
        body_profile(2.19, 0.23, 0.72, 0.83, 0.58, 0.84, 0.78, 0.76, 0.77, 0.42),
        body_profile(1.95, 0.20, 0.88, 0.92, 0.82, 0.91, 0.94, 0.84, 0.94, 0.72),
        body_profile(1.35, 0.18, 0.91, 0.94, 0.88, 0.92, 1.00, 0.84, 1.05, 0.78),
        body_profile(0.72, 0.18, 0.92, 0.95, 0.89, 0.91, 1.08, 0.84, 1.32, 0.78),
        body_profile(0.20, 0.18, 0.92, 0.95, 0.89, 0.91, 1.16, 0.84, 1.54, 0.76),
        body_profile(-0.36, 0.18, 0.92, 0.95, 0.89, 0.91, 1.15, 0.83, 1.53, 0.75),
        body_profile(-0.92, 0.18, 0.92, 0.95, 0.89, 0.90, 1.09, 0.82, 1.43, 0.72),
        body_profile(-1.30, 0.18, 0.91, 0.94, 0.88, 0.90, 1.02, 0.80, 1.33, 0.68),
        body_profile(-1.75, 0.20, 0.88, 0.91, 0.84, 0.87, 0.92, 0.76, 1.02, 0.54),
        body_profile(-2.19, 0.26, 0.76, 0.84, 0.62, 0.81, 0.78, 0.70, 0.80, 0.38),
    ]
    body.add_loft(dense_profiles(raw_profiles, 2))
    meshes.append(body)

    # Glass areas.
    glass = Mesh("glass_windows", "glass")
    glass.add_box(V3(0, 1.18, 0.42), V3(1.48, 0.015, 0.50))
    glass.add_box(V3(-0.875, 1.15, 0.30), V3(0.02, 0.42, 0.78))
    glass.add_box(V3(0.875, 1.15, 0.30), V3(0.02, 0.42, 0.78))
    glass.add_box(V3(-0.875, 1.15, -0.55), V3(0.02, 0.42, 0.68))
    glass.add_box(V3(0.875, 1.15, -0.55), V3(0.02, 0.42, 0.68))
    glass.add_box(V3(-0.84, 1.13, -1.24), V3(0.02, 0.32, 0.35))
    glass.add_box(V3(0.84, 1.13, -1.24), V3(0.02, 0.32, 0.35))
    glass.add_box(V3(0, 1.06, -1.92), V3(1.36, 0.38, 0.02))
    glass.add_box(V3(0, 1.57, -0.40), V3(1.36, 0.02, 1.05))
    meshes.append(glass)

    # Trim, grille, lights and rails.
    trim = Mesh("body_trim_grille_headlights", "dark_trim")
    trim.add_box(V3(0, 0.57, 2.205), V3(1.18, 0.34, 0.025))
    for x in [-0.42, -0.21, 0.0, 0.21, 0.42]:
        trim.add_box(V3(x, 0.57, 2.225), V3(0.025, 0.32, 0.025))
    trim.add_box(V3(0, 0.75, 2.23), V3(1.28, 0.045, 0.025))
    trim.add_box(V3(0, 0.39, 2.23), V3(1.28, 0.045, 0.025))
    for x in [-0.28, -0.09, 0.09, 0.28]:
        trim.add_torus(V3(x, 0.70, 2.245), 0.065, 0.008, axis="z", seg_major=26, seg_minor=6)
    meshes.append(trim)

    headlights = Mesh("headlights_taillights", "light_lens")
    for x in [-0.70, 0.70]:
        headlights.add_box(V3(x, 0.66, 2.19), V3(0.36, 0.105, 0.035))
        headlights.add_box(V3(x, 0.53, 2.205), V3(0.26, 0.045, 0.025))
    meshes.append(headlights)

    tails = Mesh("taillights", "tail_lens")
    for x in [-0.55, 0.55]:
        tails.add_box(V3(x, 0.77, -2.20), V3(0.48, 0.13, 0.035))
        tails.add_box(V3(x * 0.45, 0.73, -2.215), V3(0.18, 0.08, 0.025))
    meshes.append(tails)

    rails = Mesh("roof_rails", "chrome")
    for x in [-0.67, 0.67]:
        rails.add_cylinder_between(V3(x, 1.64, 0.55), V3(x, 1.64, -1.32), 0.018, 14)
        rails.add_cylinder_between(V3(x, 1.59, 0.48), V3(x, 1.64, 0.55), 0.012, 8)
        rails.add_cylinder_between(V3(x, 1.59, -1.25), V3(x, 1.64, -1.32), 0.012, 8)
    meshes.append(rails)

    # Wheels.
    wheel_x = 0.915
    meshes.append(make_wheel("wheel_fl", -wheel_x, 1.3015))
    meshes.append(make_wheel("wheel_fr", wheel_x, 1.3015))
    meshes.append(make_wheel("wheel_rl", -wheel_x, -1.3015))
    meshes.append(make_wheel("wheel_rr", wheel_x, -1.3015))

    # Interior.
    front_seats = Mesh("front_seats_dashboard", "interior_dark")
    for x in [-0.35, 0.35]:
        front_seats.add_box(V3(x, 0.72, 0.35), V3(0.38, 0.13, 0.42))
        front_seats.add_box(V3(x, 0.98, 0.20), V3(0.36, 0.47, 0.10))
    front_seats.add_box(V3(0, 0.84, 0.95), V3(1.25, 0.18, 0.18))
    front_seats.add_torus(V3(-0.36, 0.83, 1.04), 0.11, 0.012, axis="z", seg_major=26, seg_minor=6)
    meshes.append(front_seats)

    rear_seat = Mesh("rear_seat_cushion", "interior")
    rear_seat.add_box(V3(0.0, 0.82, -0.98), V3(1.34, 0.16, 0.68))
    rear_seat.add_box(V3(0.0, 1.08, -1.26), V3(1.28, 0.44, 0.12))
    meshes.append(rear_seat)

    # Fuel system internals.
    tank = Mesh("fuel_tank", "xray_internal")
    tank.add_ellipsoid(V3(-0.34, 0.40, -1.02), V3(0.34, 0.12, 0.42), 32, 12)
    tank.add_ellipsoid(V3(0.34, 0.40, -1.02), V3(0.34, 0.12, 0.42), 32, 12)
    tank.add_box(V3(0.0, 0.33, -1.02), V3(0.36, 0.11, 0.52))
    meshes.append(tank)

    cover = Mesh("fuel_access_cover", "blue_highlight")
    cover.add_cylinder_y(V3(0.34, 0.57, -0.92), 0.24, 0.035, 40)
    meshes.append(cover)

    ring = Mesh("fuel_locking_ring", "chrome")
    ring.add_torus(V3(0.34, 0.61, -0.92), 0.165, 0.016, axis="y", seg_major=36, seg_minor=8)
    meshes.append(ring)

    pump = Mesh("fuel_pump", "blue_highlight")
    pump.add_cylinder_y(V3(0.34, 0.66, -0.92), 0.105, 0.20, 32)
    pump.add_cylinder_y(V3(0.34, 0.78, -0.92), 0.065, 0.07, 24)
    pump.add_box(V3(0.40, 0.61, -0.78), V3(0.12, 0.05, 0.16))
    meshes.append(pump)

    connector = Mesh("fuel_connector", "orange")
    connector.add_box(V3(0.34, 0.76, -0.74), V3(0.16, 0.055, 0.10))
    connector.add_cylinder_between(V3(0.41, 0.74, -0.75), V3(0.56, 0.69, -0.75), 0.018, 10)
    meshes.append(connector)

    controller = Mesh("fuel_pump_controller", "orange")
    controller.add_box(V3(0.64, 0.66, -0.76), V3(0.22, 0.05, 0.16))
    meshes.append(controller)

    line = Mesh("fuel_line", "xray_internal")
    line.add_cylinder_between(V3(0.37, 0.56, -0.78), V3(0.42, 0.48, -0.20), 0.026, 14)
    line.add_cylinder_between(V3(0.42, 0.48, -0.20), V3(0.38, 0.50, 1.30), 0.026, 14)
    meshes.append(line)

    left_sender = Mesh("fuel_sender_left", "blue_highlight")
    left_sender.add_cylinder_y(V3(-0.34, 0.56, -0.92), 0.17, 0.045, 32)
    left_sender.add_cylinder_y(V3(-0.34, 0.45, -0.92), 0.055, 0.18, 18)
    meshes.append(left_sender)

    jet = Mesh("suction_jet_pump", "blue_highlight")
    jet.add_cylinder_between(V3(-0.34, 0.48, -0.78), V3(0.23, 0.54, -0.78), 0.026, 14)
    jet.add_box(V3(-0.36, 0.48, -0.78), V3(0.20, 0.07, 0.10))
    meshes.append(jet)

    # Engine bay service nodes.
    engine = Mesh("engine_block", "engine")
    engine.add_box(V3(0.05, 0.43, 1.15), V3(0.78, 0.38, 0.42))
    engine.add_box(V3(0.05, 0.68, 1.15), V3(0.72, 0.13, 0.35))
    for x in [-0.24, -0.08, 0.08, 0.24]:
        engine.add_cylinder_y(V3(x, 0.79, 1.15), 0.035, 0.055, 16)
    engine.add_box(V3(0.05, 0.72, 1.42), V3(0.60, 0.12, 0.15))
    meshes.append(engine)

    oil_filter = Mesh("oil_filter", "orange")
    oil_filter.add_cylinder_y(V3(0.28, 0.74, 1.34), 0.055, 0.15, 24)
    meshes.append(oil_filter)

    air_filter = Mesh("air_filter", "dark_trim")
    air_filter.add_box(V3(0.58, 0.72, 1.28), V3(0.36, 0.20, 0.32))
    air_filter.add_cylinder_between(V3(0.44, 0.68, 1.12), V3(0.12, 0.58, 0.88), 0.045, 14)
    meshes.append(air_filter)

    turbo = Mesh("turbo", "blue_highlight")
    turbo.add_torus(V3(0.08, 0.40, 0.82), 0.075, 0.030, axis="z", seg_major=24, seg_minor=8)
    turbo.add_cylinder_between(V3(0.08, 0.33, 0.78), V3(0.08, 0.22, 0.62), 0.04, 14)
    meshes.append(turbo)

    exhaust = Mesh("exhaust_manifold", "orange")
    for x in [-0.25, -0.08, 0.08, 0.25]:
        exhaust.add_cylinder_between(V3(x, 0.43, 0.98), V3(0.08, 0.39, 0.84), 0.022, 10)
    meshes.append(exhaust)

    transmission = Mesh("transmission", "engine")
    transmission.add_ellipsoid(V3(-0.48, 0.32, 1.03), V3(0.25, 0.22, 0.28), 24, 10)
    meshes.append(transmission)

    shafts = Mesh("drive_shaft", "chrome")
    shafts.add_cylinder_between(V3(-0.48, 0.34, 1.03), V3(-0.86, 0.34, 1.30), 0.035, 16)
    shafts.add_cylinder_between(V3(-0.48, 0.34, 1.03), V3(0.86, 0.34, 1.30), 0.035, 16)
    shafts.add_cylinder_between(V3(0.0, 0.28, 0.75), V3(0.0, 0.28, -1.35), 0.035, 16)
    meshes.append(shafts)

    return meshes


MATERIALS: dict[str, dict] = {
    "xray_shell": {
        "baseColorFactor": [0.78, 0.88, 0.98, 0.34],
        "metallicFactor": 0.0,
        "roughnessFactor": 0.38,
        "alphaMode": "BLEND",
        "doubleSided": True,
    },
    "xray_internal": {
        "baseColorFactor": [0.58, 0.70, 0.78, 0.72],
        "metallicFactor": 0.0,
        "roughnessFactor": 0.46,
        "alphaMode": "BLEND",
        "doubleSided": True,
    },
    "glass": {
        "baseColorFactor": [0.08, 0.14, 0.22, 0.46],
        "metallicFactor": 0.0,
        "roughnessFactor": 0.12,
        "alphaMode": "BLEND",
        "doubleSided": True,
    },
    "dark_trim": {
        "baseColorFactor": [0.02, 0.025, 0.03, 1.0],
        "metallicFactor": 0.0,
        "roughnessFactor": 0.55,
        "doubleSided": True,
    },
    "chrome": {
        "baseColorFactor": [0.78, 0.78, 0.76, 1.0],
        "metallicFactor": 0.75,
        "roughnessFactor": 0.22,
        "doubleSided": True,
    },
    "rubber": {
        "baseColorFactor": [0.025, 0.025, 0.025, 1.0],
        "metallicFactor": 0.0,
        "roughnessFactor": 0.78,
        "doubleSided": True,
    },
    "rim": {
        "baseColorFactor": [0.82, 0.82, 0.80, 1.0],
        "metallicFactor": 0.5,
        "roughnessFactor": 0.32,
        "doubleSided": True,
    },
    "interior": {
        "baseColorFactor": [0.30, 0.34, 0.39, 1.0],
        "metallicFactor": 0.0,
        "roughnessFactor": 0.62,
        "doubleSided": True,
    },
    "interior_dark": {
        "baseColorFactor": [0.08, 0.09, 0.11, 1.0],
        "metallicFactor": 0.0,
        "roughnessFactor": 0.64,
        "doubleSided": True,
    },
    "blue_highlight": {
        "baseColorFactor": [0.35, 0.75, 0.92, 0.88],
        "metallicFactor": 0.0,
        "roughnessFactor": 0.30,
        "alphaMode": "BLEND",
        "doubleSided": True,
    },
    "orange": {
        "baseColorFactor": [1.0, 0.55, 0.10, 1.0],
        "metallicFactor": 0.0,
        "roughnessFactor": 0.36,
        "doubleSided": True,
    },
    "engine": {
        "baseColorFactor": [0.46, 0.52, 0.58, 1.0],
        "metallicFactor": 0.12,
        "roughnessFactor": 0.46,
        "doubleSided": True,
    },
    "light_lens": {
        "baseColorFactor": [0.88, 0.95, 1.0, 0.86],
        "metallicFactor": 0.0,
        "roughnessFactor": 0.08,
        "alphaMode": "BLEND",
        "doubleSided": True,
    },
    "tail_lens": {
        "baseColorFactor": [1.0, 0.08, 0.02, 0.86],
        "metallicFactor": 0.0,
        "roughnessFactor": 0.14,
        "alphaMode": "BLEND",
        "doubleSided": True,
    },
}


def align4(data: bytearray, pad: int = 0) -> None:
    while len(data) % 4:
        data.append(pad)


def pack_f32(values: Iterable[float]) -> bytes:
    values = list(values)
    return struct.pack("<" + "f" * len(values), *values)


def pack_u16(values: Iterable[int]) -> bytes:
    values = list(values)
    return struct.pack("<" + "H" * len(values), *values)


def build_glb(meshes: list[Mesh], out_path: Path) -> None:
    material_names = list(MATERIALS.keys())
    materials = []
    for name in material_names:
        src = MATERIALS[name]
        mat = {
            "name": name,
            "pbrMetallicRoughness": {
                "baseColorFactor": src["baseColorFactor"],
                "metallicFactor": src.get("metallicFactor", 0.0),
                "roughnessFactor": src.get("roughnessFactor", 0.5),
            },
            "doubleSided": src.get("doubleSided", True),
        }
        if "alphaMode" in src:
            mat["alphaMode"] = src["alphaMode"]
        materials.append(mat)
    material_index = {name: i for i, name in enumerate(material_names)}

    blob = bytearray()
    buffer_views = []
    accessors = []
    gltf_meshes = []
    nodes = []

    def add_view(data: bytes, target: int) -> int:
        align4(blob)
        offset = len(blob)
        blob.extend(data)
        align4(blob)
        buffer_views.append(
            {
                "buffer": 0,
                "byteOffset": offset,
                "byteLength": len(data),
                "target": target,
            }
        )
        return len(buffer_views) - 1

    for mesh in meshes:
        if not mesh.positions:
            continue
        if len(mesh.positions) > 65535:
            raise ValueError(f"{mesh.name} has too many vertices for uint16")
        pos_flat = [c for p in mesh.positions for c in p.as_tuple()]
        norm_flat = [c for n in mesh.normals for c in n.as_tuple()]
        idx_flat = mesh.indices

        pos_view = add_view(pack_f32(pos_flat), 34962)
        norm_view = add_view(pack_f32(norm_flat), 34962)
        idx_view = add_view(pack_u16(idx_flat), 34963)

        xs = [p.x for p in mesh.positions]
        ys = [p.y for p in mesh.positions]
        zs = [p.z for p in mesh.positions]
        pos_accessor = {
            "bufferView": pos_view,
            "byteOffset": 0,
            "componentType": 5126,
            "count": len(mesh.positions),
            "type": "VEC3",
            "min": [min(xs), min(ys), min(zs)],
            "max": [max(xs), max(ys), max(zs)],
        }
        norm_accessor = {
            "bufferView": norm_view,
            "byteOffset": 0,
            "componentType": 5126,
            "count": len(mesh.normals),
            "type": "VEC3",
        }
        idx_accessor = {
            "bufferView": idx_view,
            "byteOffset": 0,
            "componentType": 5123,
            "count": len(idx_flat),
            "type": "SCALAR",
        }
        accessors.extend([pos_accessor, norm_accessor, idx_accessor])
        p_i, n_i, idx_i = len(accessors) - 3, len(accessors) - 2, len(accessors) - 1

        gltf_meshes.append(
            {
                "name": mesh.name,
                "primitives": [
                    {
                        "attributes": {"POSITION": p_i, "NORMAL": n_i},
                        "indices": idx_i,
                        "material": material_index[mesh.material],
                    }
                ],
            }
        )
        nodes.append({"name": mesh.name, "mesh": len(gltf_meshes) - 1})

    gltf = {
        "asset": {
            "version": "2.0",
            "generator": "TechGid procedural Audi Q3 GLB generator",
        },
        "scene": 0,
        "scenes": [{"name": "Audi Q3 8U service scene", "nodes": list(range(len(nodes)))}],
        "nodes": nodes,
        "meshes": gltf_meshes,
        "materials": materials,
        "buffers": [{"byteLength": len(blob)}],
        "bufferViews": buffer_views,
        "accessors": accessors,
    }

    json_bytes = json.dumps(gltf, separators=(",", ":"), ensure_ascii=False).encode("utf-8")
    json_chunk = bytearray(json_bytes)
    align4(json_chunk, 0x20)
    bin_chunk = bytearray(blob)
    align4(bin_chunk)

    total_len = 12 + 8 + len(json_chunk) + 8 + len(bin_chunk)
    out = bytearray()
    out.extend(struct.pack("<III", 0x46546C67, 2, total_len))
    out.extend(struct.pack("<I4s", len(json_chunk), b"JSON"))
    out.extend(json_chunk)
    out.extend(struct.pack("<I4s", len(bin_chunk), b"BIN\0"))
    out.extend(bin_chunk)

    out_path.parent.mkdir(parents=True, exist_ok=True)
    out_path.write_bytes(out)


def write_preview(path: Path) -> None:
    try:
        from PIL import Image, ImageDraw, ImageFilter
    except Exception:
        return

    size = 640
    img = Image.new("RGBA", (size, size), (247, 250, 252, 255))
    draw = ImageDraw.Draw(img, "RGBA")
    shadow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    sd = ImageDraw.Draw(shadow, "RGBA")
    sd.ellipse((125, 430, 535, 500), fill=(35, 50, 70, 55))
    shadow = shadow.filter(ImageFilter.GaussianBlur(18))
    img.alpha_composite(shadow)
    body = [(105, 360), (160, 285), (270, 235), (420, 245), (520, 318), (548, 380), (505, 415), (155, 410)]
    draw.polygon(body, fill=(208, 224, 238, 118), outline=(105, 142, 172, 175))
    draw.line([(160, 285), (250, 320), (390, 325), (520, 318)], fill=(110, 155, 190, 120), width=3)
    draw.polygon([(245, 250), (315, 238), (395, 250), (440, 315), (220, 315)], fill=(30, 50, 76, 90))
    for x in (210, 455):
        draw.ellipse((x - 48, 372, x + 48, 468), fill=(20, 24, 30, 255))
        draw.ellipse((x - 27, 393, x + 27, 447), fill=(205, 211, 216, 255))
    draw.rectangle((132, 342, 240, 370), fill=(20, 28, 36, 230))
    draw.rectangle((410, 335, 510, 353), fill=(66, 180, 230, 120))
    draw.text((94, 108), "Audi Q3 8U", fill=(33, 42, 54, 255))
    draw.text((94, 140), "2.0 TFSI service x-ray GLB", fill=(96, 111, 128, 255))
    img.save(path)


def main() -> None:
    meshes = build_model_meshes()
    build_glb(meshes, OUT_GLB)
    write_preview(OUT_PREVIEW)
    total_vertices = sum(len(m.positions) for m in meshes)
    total_faces = sum(len(m.indices) // 3 for m in meshes)
    print(f"wrote {OUT_GLB}")
    print(f"meshes={len(meshes)} vertices={total_vertices} triangles={total_faces} size={OUT_GLB.stat().st_size}")
    if OUT_PREVIEW.exists():
        print(f"wrote {OUT_PREVIEW}")


if __name__ == "__main__":
    main()
