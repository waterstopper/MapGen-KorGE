# Korge mapgen

This is a korge map generator for [Pocket Palm Heroes remastered](https://github.com/SerVB/pph).

## Algorithm

Is based mostly on [this presentation](https://en.ppt-online.org/29091) by Gus Smedstad, who is the HoMM3 programmer.

5 steps:

- [x] Parse a map template file (examples are in `/resources`)
- [x] Place circles with connecting segments
- [x] Create Voronoi Diagram from circle centers 
- [x] Restructure diagram into a grid of cells, where each cell represents an empty tile. 
- [x] Add edges between zones
- [x] Add castles and mines in each zone
- [x] Create passages and portals between zones
- [x] Connect castles and mines with roads
- [ ] Populate all tiles (obviously the most complex step, but for now it is enough to get the basic idea)

Here is how an incomplete second step looks like:

![](images/map.png)
![](images/dense.png)
![](images/sizes.png)
![](images/normal.png)