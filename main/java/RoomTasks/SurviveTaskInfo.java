package RoomTasks;

import java.util.ArrayList;

public class SurviveTaskInfo extends RoomTaskInfo {
    private int surviveTime;
    private ArrayList<String> mobs;

    public SurviveTaskInfo (int surviveTime) {
        this.surviveTime = surviveTime;
        mobs = new ArrayList<>();
    }
    public void addMob (String mob) {
        mobs.add(mob);
    }
    public int getSurviveTime () {
        return surviveTime;
    }
    public ArrayList<String> getMobs () {
        return mobs;
    }
}
