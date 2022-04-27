# Issues

1. Step 8 will be executed incorrectly if buildings separate two parts inside zone.

Example: white zone is separated by buildings

![](resources/placedBuildings.png)

2. There are 3 types of portals (and 6 types of one-way teleports), so creating more than 6 portals on the map is
   impossible (3 portals and 3 pairs of one-way teleports). This issue is at the core of PPH and should be fixed there.