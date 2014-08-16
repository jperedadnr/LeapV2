package leapv2;

import com.leapmotion.leap.Arm;
import com.leapmotion.leap.Bone;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.FingerList;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
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
    private final List<Arm> arms=new ArrayList<>();
    private final List<Pair> joints=new ArrayList<>();
    
    @Override
    public void onFrame(Controller controller) {
        Frame frame = controller.frame();
        doneList.set(false);
        bones.clear();
        arms.clear();
        joints.clear();
        if (!frame.hands().isEmpty()) {
            Screen screen = controller.locatedScreens().get(0);
            if (screen != null && screen.isValid()){
                for(Finger finger : frame.fingers()){
                    if(finger.isValid()){
                        for(Bone.Type b : Bone.Type.values()) {
                            Bone bone = finger.bone(b);
                            if((!finger.type().equals(Finger.Type.TYPE_RING) && 
                                !finger.type().equals(Finger.Type.TYPE_MIDDLE)) || 
                                !b.equals(Bone.Type.TYPE_METACARPAL)){
                                bones.add(bone);
                            }
                        }
                    }
                }
                for(Hand h: frame.hands()){
                    if(h.isValid()){
                        // arm
                        arms.add(h.arm());
                        
                        FingerList fingers = h.fingers();
                        Finger index=null, middle=null, ring=null, pinky=null;
                        for(Finger f: fingers){
                            if(f.isFinger() && f.isValid()){
                                switch(f.type()){
                                    case TYPE_INDEX: index=f; break;
                                    case TYPE_MIDDLE: middle=f; break;
                                    case TYPE_RING: ring=f; break;
                                    case TYPE_PINKY: pinky=f; break;
                                }
                            }
                        }
                        // joints
                        if(index!=null && middle!=null){
                            Pair p=new Pair(index.bone(Bone.Type.TYPE_METACARPAL).nextJoint(),
                                            middle.bone(Bone.Type.TYPE_METACARPAL).nextJoint());
                            joints.add(p);
                        }
                        if(middle!=null && ring!=null){
                            Pair p=new Pair(middle.bone(Bone.Type.TYPE_METACARPAL).nextJoint(),
                                            ring.bone(Bone.Type.TYPE_METACARPAL).nextJoint());
                            joints.add(p);
                        }
                        if(ring!=null && pinky!=null){
                            Pair p=new Pair(ring.bone(Bone.Type.TYPE_METACARPAL).nextJoint(),
                                            pinky.bone(Bone.Type.TYPE_METACARPAL).nextJoint());
                            joints.add(p);
                        }
                        if(index!=null && pinky!=null){
                            Pair p=new Pair(index.bone(Bone.Type.TYPE_METACARPAL).prevJoint(),
                                            pinky.bone(Bone.Type.TYPE_METACARPAL).prevJoint());
                            joints.add(p);
                        }        
                    }
                }
            }
        }
        
        doneList.set(!bones.isEmpty() || !arms.isEmpty());
    }
    
    public List<Bone> getBones(){ 
        // Returns a fresh copy of the bones collection 
        // to avoid concurrent exceptions iterating this list
        return bones.stream().collect(Collectors.toList());
    }
    public List<Arm> getArms(){ 
        // Returns a fresh copy of the arms collection 
        // to avoid concurrent exceptions iterating this list
        return arms.stream().collect(Collectors.toList());
    }
    public List<Pair> getJoints(){ 
        // Returns a fresh copy of the joints collection 
        // to avoid concurrent exceptions iterating this list
        return joints.stream().collect(Collectors.toList());
    }
    
    public BooleanProperty doneListProperty() { 
        return doneList; 
    }
    
}
