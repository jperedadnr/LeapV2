package leapv2;

import com.leapmotion.leap.Arm;
import com.leapmotion.leap.Bone;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Vector;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

/**
 *
 * @author Jose Pereda - June 2014 - @JPeredaDnr
 */
public class LeapV2 extends Application {
    
    private LeapListener listener = null;
    private Controller controller = null;
    private final Rotate cameraXRotate = new Rotate(0,0,0,0,Rotate.X_AXIS);
    private final Rotate cameraYRotate = new Rotate(0,0,0,0,Rotate.Y_AXIS);
    private final Translate cameraPosition = new Translate(-100,-550,-200);
    private Shape3D[] meshView=null;
    private final Group root=new Group();
    private double dragStartX, dragStartY, dragStartRotateX, dragStartRotateY;
    
    @Override
    public void start(Stage primaryStage) {
        listener = new LeapListener();
        controller = new Controller();
        controller.addListener(listener);
        AnchorPane pane=new AnchorPane();
        
        Scene scene = new Scene(pane, 800, 600, Color.BEIGE);
        final PerspectiveCamera camera = new PerspectiveCamera();
        camera.setFieldOfView(90);
        camera.getTransforms().addAll(cameraXRotate,cameraYRotate,cameraPosition);

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.GAINSBORO);
        material.setSpecularColor(Color.rgb(30, 30, 30));
        
        meshView = new Shape3D[2];
        meshView[0]=new MeshView(buildTriangleMesh(20, 20, 30,true));
        meshView[1]=new MeshView(buildTriangleMesh(20, 20, 30,false));
        for (int i=0; i<2; i++) {
            meshView[i].setMaterial(material);
            meshView[i].setTranslateX(300);
            meshView[i].setTranslateY(i==0?-300:0);
            meshView[i].setTranslateZ(i==0?0:-300);
            meshView[i].setDrawMode(DrawMode.LINE);
            meshView[i].setCullFace(CullFace.BACK);
        }
        final Group parent = new Group(meshView);
        root.getChildren().addAll(parent);
        final PointLight pointLight = new PointLight(Color.ANTIQUEWHITE);
        pointLight.setTranslateX(800);
        pointLight.setTranslateY(-800);
        pointLight.setTranslateZ(-600);
        Cylinder axisX=new Cylinder(5,700);
        axisX.setRotationAxis(Rotate.Z_AXIS);
        axisX.setRotate(90d);
        axisX.setTranslateX(250d);
        root.getChildren().add(axisX);
        Cylinder axisY=new Cylinder(5,700);
        axisY.setTranslateY(-250d);
        root.getChildren().add(axisY);
        Cylinder axisZ=new Cylinder(5,700);
        axisZ.setRotationAxis(Rotate.X_AXIS);
        axisZ.setRotate(90d);
        axisZ.setTranslateZ(-250d);
        root.getChildren().add(axisZ);
        root.getChildren().addAll(pointLight);
        
        Group root3D=new Group();
        root3D.getChildren().addAll(camera, root);
        SubScene subScene = new SubScene(root3D, 800, 600,true,SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);

        pane.getChildren().addAll(subScene);
        
        final int minRoot=root.getChildren().size();
        final PhongMaterial materialFinger = new PhongMaterial();
        materialFinger.setDiffuseColor(Color.GOLDENROD);
        materialFinger.setSpecularColor(Color.rgb(50, 50, 50));
        final PhongMaterial materialArm = new PhongMaterial();
        materialArm.setDiffuseColor(Color.CORNSILK);
        materialArm.setSpecularColor(Color.rgb(30, 30, 30));

        listener.doneListProperty().addListener((ov,b,b1)->{
            if(b1){
                
                // First, get a fresh copy of the bones, arms & joints collection
                List<Bone> bones=listener.getBones();
                List<Arm> arms=listener.getArms();
                List<Pair> joints=listener.getJoints();
                
                Platform.runLater(()->{
                    // Now, on the JavaFX thread
                    if(root.getChildren().size()>minRoot){
                        // clean old bones
                        root.getChildren().remove(minRoot,root.getChildren().size()-1);
                    }
                    
                    // Iterate over the list adding the bones to the scene
                    // If the collection changes there won't be any concurrent exception
                    // as we are iterating over its copy.
                    bones.stream()
                        .filter((bone) -> (bone.isValid() && bone.length()>0))
                        .forEach((bone) -> {
                            final Vector p=bone.center();
                            // create bone as a vertical cylinder and locate it at its center position
                            Cylinder c=new Cylinder(bone.width()/2,bone.length());
                            c.setMaterial(materialFinger);
                            
                            // translate and rotate the cylinder towards its direction
                            final Vector v=bone.direction();
                            Vector cross = (new Vector(v.getX(),-v.getY(),-v.getZ())).cross(new Vector(0,-1,0));
                            double ang=(new Vector(v.getX(),-v.getY(),-v.getZ())).angleTo(new Vector(0,-1,0));
                            c.getTransforms().addAll(
                                    new Translate(200+p.getX(),-p.getY(),-p.getZ()),
                                    new Rotate(-Math.toDegrees(ang), 
                                        0, 0, 0, 
                                        new Point3D(cross.getX(),-cross.getY(),cross.getZ()))
                                    );
                            
                            // add bone to scene
                            root.getChildren().add(c);
                            
                            Sphere s=new Sphere(bone.width()/2f*1.2);
                            s.setMaterial(materialFinger);
                            s.getTransforms().addAll(
                                    new Translate(200+p.getX(),-p.getY()+bone.length()/2d,-p.getZ()),
                                    new Rotate(-Math.toDegrees(ang), 
                                        0, -bone.length()/2d, 0, 
                                        new Point3D(cross.getX(),-cross.getY(),cross.getZ())));
                            
                            // add bone to scene
                            root.getChildren().add(s);
                            
                            Sphere s2=new Sphere(bone.width()/2f*1.2);
                            s2.setMaterial(materialFinger);
                            s2.getTransforms().addAll(
                                    new Translate(200+p.getX(),-p.getY()-bone.length()/2d,-p.getZ()),
                                    new Rotate(-Math.toDegrees(ang), 
                                        0, bone.length()/2d, 0, 
                                        new Point3D(cross.getX(),-cross.getY(),cross.getZ())));
                            
                            // add bone to scene
                            root.getChildren().add(s2);
                            
                        });
                     
                    arms.stream()
                        .filter(arm->arm.isValid())
                        .forEach(arm->{
                            final Vector p=arm.center();
                            // create bone as a cylinder and locate it at its center position
                            Cylinder c=new Cylinder(arm.width()/2,arm.elbowPosition().minus(arm.wristPosition()).magnitude());
                            c.setMaterial(materialArm);

                            // rotate the cylinder towards its direction
                            final Vector v=arm.direction();
                            Vector cross = (new Vector(v.getX(),-v.getY(),-v.getZ())).cross(new Vector(0,-1,0));
                            double ang=(new Vector(v.getX(),-v.getY(),-v.getZ())).angleTo(new Vector(0,-1,0));
                            c.getTransforms().addAll(
                                    new Translate(200+p.getX(),-p.getY(),-p.getZ()),
                                    new Rotate(-Math.toDegrees(ang), 
                                        0, 0, 0, 
                                        new Point3D(cross.getX(),-cross.getY(),cross.getZ()))
                                    );
                            
                            // add arm to scene
                            root.getChildren().add(c);
                        });
                    
                    joints.stream()
                        .forEach(joint->{
                            double length=joint.getV0().distanceTo(joint.getV1());
                            Cylinder c=new Cylinder(bones.get(0).width()/4,length);
                            c.setMaterial(materialArm);
                            
                            final Vector p=joint.getCenter();
                            final Vector v=joint.getDirection();
                            Vector cross = (new Vector(v.getX(),-v.getY(),-v.getZ())).cross(new Vector(0,-1,0));
                            double ang=(new Vector(v.getX(),-v.getY(),-v.getZ())).angleTo(new Vector(0,-1,0));
                            c.getTransforms().addAll(
                                    new Translate(200+p.getX(),-p.getY(),-p.getZ()),
                                    new Rotate(-Math.toDegrees(ang), 
                                        0, 0, 0, 
                                        new Point3D(cross.getX(),-cross.getY(),cross.getZ()))
                                    );
                            
                                                        
                            // add joint to scene
                            root.getChildren().add(c);
                        });
                });
            }
        });
        
        scene.addEventHandler(MouseEvent.ANY, event -> {
            if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                dragStartX = event.getSceneX();
                dragStartY = event.getSceneY();
                dragStartRotateX = cameraXRotate.getAngle();
                dragStartRotateY = cameraYRotate.getAngle();
            } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                double xDelta = event.getSceneX() -  dragStartX;
                double yDelta = event.getSceneY() -  dragStartY;
                cameraXRotate.setAngle(dragStartRotateX - (yDelta*0.7));
                cameraYRotate.setAngle(dragStartRotateY + (xDelta*0.7));
            }
        });
        
        primaryStage.setTitle("Skeletal Tracking with Leap Motion v2 and JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop(){
        controller.removeListener(listener);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    final static float minX = -10;
    final static float minY = -10;
    final static float maxX = 10;
    final static float maxY = 10;
 
    public TriangleMesh buildTriangleMesh(int subDivX, int subDivY, float scale, boolean planeXY) {
 
        final int pointSize = 3;
        final int texCoordSize = 2;
        // 3 point indices and 3 texCoord indices per triangle
        final int faceSize = 6;
        int numDivX = subDivX + 1;
        int numVerts = (subDivY + 1) * numDivX;
        float points[] = new float[numVerts * pointSize];
        float texCoords[] = new float[numVerts * texCoordSize];
        int faceCount = subDivX * subDivY * 2;
        int faces[] = new int[faceCount * faceSize];
 
        // Create points and texCoords
        for (int y = 0; y <= subDivY; y++) {
            float dy = (float) y / subDivY;
            double fy = (1 - dy) * minY + dy * maxY;
 
            for (int x = 0; x <= subDivX; x++) {
                float dx = (float) x / subDivX;
                double fx = (1 - dx) * minX + dx * maxX;
 
                int index = y * numDivX * pointSize + (x * pointSize);
                points[index] = (float) fx * scale;
                if(planeXY){
                    points[index + 1] = (float) fy * scale;
                    points[index + 2] = 0.0f;
                } else {
                    points[index + 2] = (float) fy * scale;
                    points[index + 1] = 0.0f;
                }
 
                index = y * numDivX * texCoordSize + (x * texCoordSize);
                texCoords[index] = dx;
                texCoords[index + 1] = dy;
            }
        }
 
        // Create faces
        for (int y = 0; y < subDivY; y++) {
            for (int x = 0; x < subDivX; x++) {
                int p00 = y * numDivX + x;
                int p01 = p00 + 1;
                int p10 = p00 + numDivX;
                int p11 = p10 + 1;
                int tc00 = y * numDivX + x;
                int tc01 = tc00 + 1;
                int tc10 = tc00 + numDivX;
                int tc11 = tc10 + 1;
 
                int index = (y * subDivX * faceSize + (x * faceSize)) * 2;
                faces[index + 0] = p00;
                faces[index + 1] = tc00;
                if(planeXY){
                    faces[index + 2] = p10;
                    faces[index + 3] = tc10;
                    faces[index + 4] = p11;
                    faces[index + 5] = tc11;
                } else {
                    faces[index + 2] = p11;
                    faces[index + 3] = tc11;
                    faces[index + 4] = p10;
                    faces[index + 5] = tc10;
                }
 
                index += faceSize;
                faces[index + 0] = p11;
                faces[index + 1] = tc11;
                if(planeXY){
                    faces[index + 2] = p01;
                    faces[index + 3] = tc01;
                    faces[index + 4] = p00;
                    faces[index + 5] = tc00;
                } else {
                    faces[index + 2] = p00;
                    faces[index + 3] = tc00;
                    faces[index + 4] = p01;
                    faces[index + 5] = tc01;
                }
            }
        }
 
        TriangleMesh triangleMesh = new TriangleMesh();
        triangleMesh.getPoints().addAll(points);
        triangleMesh.getTexCoords().addAll(texCoords);
        triangleMesh.getFaces().addAll(faces);
 
        return triangleMesh;
    }
    
}
