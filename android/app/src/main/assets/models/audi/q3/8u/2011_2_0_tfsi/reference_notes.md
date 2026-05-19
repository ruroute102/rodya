# Audi Q3 8U 2011 2.0 TFSI reference notes

These notes map the current repair-focused 3D scene to the local manuals used
while building the fallback model. The app must not reproduce manual pages; the
notes only keep the modeling decisions traceable.

## Geometry

- `audi_q3_rus.pdf`, page 5: base vehicle proportions for the procedural body:
  length 4385 mm, width 1831 mm, wheelbase 2603 mm, front/rear track
  1571/1575 mm, height 1590 mm without roof antenna.
- `SM_7 (12).pdf`, Fuel supply system, petrol engines: fuel tank is represented
  as a saddle tank with a right-side fuel delivery unit and a left-side sender.

## Fuel pump service scene

- Main module: right-side fuel delivery unit with fuel system pressurisation
  pump `G6`.
- Related visible service parts: access cover, locking ring, dry seal position,
  electrical connector, fuel supply line, fuel pump control unit `J538`.
- Left chamber support: fuel gauge sender `G169` and suction-jet pump transferring
  fuel from the left chamber into the delivery unit housing.
- Rear bench workflow: move front seats forward, remove the rear seat bench,
  unclip the right flange cover, disconnect connector/fuel line, release locking
  ring, lift the module out through the tank opening.

## Modeling caveat

The current asset is still a mobile-friendly procedural fallback, not the final
premium GLB. It now carries the correct service topology so a Blender/GLB model
can reuse the same node names and instruction flow.
