package leapv2;

import com.leapmotion.leap.Bone;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.Screen;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Jose Pereda - June 2014 -  @JPeredaDnr
*/
public class LeapListener extends Listener {
    
    private final BooleanProperty done= new SimpleBooleanProperty(false);
    private final ObservableList<Bone> bones=FXCollections.observableArrayList();
    
    @Override
    public void onFrame(Controller controller) {
        Frame frame = controller.frame();
        done.set(false);
        bones.clear();
        if (!frame.hands().isEmpty()) {
            Screen screen = controller.locatedScreens().get(0);
            if (screen != null && screen.isValid()){
                for(Finger finger : frame.fingers()){
                    if(finger.isValid()){
                        for(Bone.Type b : Bone.Type.values()) {
                            Bone bone = finger.bone(b);
                            bones.add(bone);
                        }
                    }
                }
            }
        }
        done.set(bones.size()>0);
    }
    
    public ObservableList<Bone> getTipBones(){ return bones; }
    public BooleanProperty doneProperty() { return done; }
    
}
