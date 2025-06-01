package RoomTasks;

import java.util.ArrayList;

public class KillTaskInfo extends RoomTaskInfo {
    int killsRequired;
    private ArrayList<String> mobs;

    public KillTaskInfo (int killsRequired) {
        this.killsRequired = killsRequired;
        mobs = new ArrayList<>();
    }
    public void addMob (String mob) {
        mobs.add(mob);
    }
    public int getKillsRequired () {
        return killsRequired;
    }
    public ArrayList<String> getMobs () {
        return mobs;
    }
}
