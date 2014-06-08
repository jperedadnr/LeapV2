package leapv2;

import com.leapmotion.leap.Bone;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.Screen;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Jose Pereda - June 2014 -  @JPeredaDnr
*/
public class LeapListener extends Listener {
    
    private final BooleanProperty doneList= new SimpleBooleanProperty(false);
    // Since we'll be listening to changes only in doneList, we don't need
    // bones collection to be observable too
    private final List<Bone> bones=new ArrayList<>();
    
    @Override
    public void onFrame(Controller controller) {
        Frame frame = controller.frame();
        doneList.set(false);
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
        doneList.set(bones.size()>0);
    }
    
    public List<Bone> getBones(){ 
        // Returns a fresh copy of the bones collection 
        // to avoid concurrent exceptions iterating this list
        return bones.stream().collect(Collectors.toList());
    }
    
    public BooleanProperty doneListProperty() { 
        return doneList; 
    }
    
}
