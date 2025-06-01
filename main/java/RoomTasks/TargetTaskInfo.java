package RoomTasks;

import Mobs.WumpusMob;
import Mobs.WumpusMobManager;

import java.util.ArrayList;

public class TargetTaskInfo extends RoomTaskInfo {
    private String targetMobName;

    public TargetTaskInfo (String targetMobName) {
        this.targetMobName = targetMobName;
    }
    public String getTargetMobName () {
        return targetMobName;
    }
}
