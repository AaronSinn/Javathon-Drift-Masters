package CodesAS2800;
/*
Group Project For COMP-2800
Members:
        Aaron Sinn
        Amir Marie
        Helia Hedayati
        Julian Samonte
       	Patrycia Lim
 */

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jdesktop.j3d.examples.collision.Box;
import org.jdesktop.j3d.examples.sound.PointSoundBehavior;
import org.jdesktop.j3d.examples.sound.audio.JOALMixer;
import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.Behavior;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.ImageComponent2D;
import org.jogamp.java3d.Link;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.PointSound;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.SharedGroup;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.Texture2D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.TransformInterpolator;
import org.jogamp.java3d.View;
import org.jogamp.java3d.ViewPlatform;
import org.jogamp.java3d.WakeupCriterion;
import org.jogamp.java3d.WakeupOnCollisionEntry;
import org.jogamp.java3d.WakeupOnCollisionExit;
import org.jogamp.java3d.WakeupOnCollisionMovement;

import java.util.*;
import org.jogamp.java3d.loaders.IncorrectFormatException;
import org.jogamp.java3d.loaders.ParsingErrorException;
import org.jogamp.java3d.loaders.Scene;
import org.jogamp.java3d.loaders.objectfile.ObjectFile;
import org.jogamp.java3d.utils.behaviors.vp.OrbitBehavior;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.geometry.Text2D;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.java3d.utils.picking.PickResult;
import org.jogamp.java3d.utils.picking.PickTool;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.Viewer;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.AxisAngle4f;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Matrix3d;
import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

import com.jogamp.graph.font.Font;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class RaceGame extends JPanel implements KeyListener, MouseListener {
	private static final long serialVersionUID = 1L;
	private static JFrame frame;
	private static final int OBJ_NUM = 6;
	private int carColourItterator = 0;
	private static Shape3D carShape3D = null;
	private static Transform3D car3D = null;
	private static TransformGroup carTG = null;
	
	//used to tell which checkpoints have been hit
	private static boolean isCheck1Hit = false;
	private static boolean isCheck2Hit = false;
	private static boolean isCheck3Hit = false;
	private static boolean isCheck4Hit = false;
	private static boolean isRaceStarted = false;
	
	private static Text2D text2d;
	private static Text2D text2d_2;	
	
	private static CollisionDetect cd;
	
	private static Canvas3D canvas;				
	private static PickTool pickTool;	//for mouse clicking
	
	private static long startTime = System.currentTimeMillis();
	private static long lapTime = 0;
	
	//for sound
	private static PointSound carMovingPS = new PointSound();
	private static PointSound carIdlePS = new PointSound();
	private static PointSound crashPS = new PointSound();
	private static boolean isFirstKeyPressed = false;				//a different sound will play depending if the car is moving or not
	
	//for car movement
	private static Transform3D t3dstep = new Transform3D();			 // car transformation
	private final static HashSet<Integer> pressedKeys = new HashSet<>();	 // multiple keys pressed ex: AS || AW
	static Vector3f startPos = new Vector3f(0.0f, 0.0f, 0.0f);		// smoother movement based on time instead
	static Vector3f endPos = new Vector3f(0.0f, 0.0f, 0.0f);	
	// static Vector3f currPos = new Vector3f(startPos);
	private float carX = 0.08f; 										// speed of car
	private static Boolean isRotation = false; 						// enable rotation
	private static Boolean right = false;		
	
	private static SimpleUniverse su = null;
	
	//follow car 
	private static Boolean isFollow = false;

	/* a function to build the content branch */	
	public static BranchGroup Shapes() {
		//Groups
		BranchGroup objBG = new BranchGroup();
		TransformGroup sceneTG = new TransformGroup();
		TransformGroup holdCarTG = new TransformGroup();
		carTG = new TransformGroup();
		car3D = new Transform3D();
		carTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		holdCarTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		
		t3dstep.setTranslation(new Vector3f(0, 0.3f, -4));
		holdCarTG.setTransform(t3dstep);
		
	
		//Shape color
		Appearance app = new Appearance();
		app.setTexture(texturedApp("images/racetrack.png"));
		
		//Background image
		Background back = new Background();
		BoundingSphere bounds = new BoundingSphere( new Point3d(0.0, 0.0, 0.0), Double.MAX_VALUE);
        sceneTG.addChild(createBkground(CommonsJS.Green, bounds));
		back.setApplicationBounds(bounds);
		sceneTG.addChild(back);
		
		//Shapes
		TrackObjects[] to = new TrackObjects[OBJ_NUM];
		to[0] = new track("Objects/car.obj", "car", CommonsJS.obj_Appearance(CommonsJS.Blue), 0f, 0f, 0f, .5f, .5f, .5f);
		carTG.addChild(to[0].position_Object());       
		holdCarTG.addChild(carTG);
		
		to[1] = new track("Objects/racetrack.obj", "track", app, 0f, 0f, 0f, 12f, 12f, 12f);
		
		to[2] = new track("Objects/obstacle.obj", "obstacle", CommonsJS.obj_Appearance(CommonsJS.Orange), 0f, 0.1f, 0f, .3f, .3f, .3f);
		
		to[3] = new track("Objects/checkpoint.obj", "checkpoint", CommonsJS.obj_Appearance(CommonsJS.Cyan), 0f, 0f, 0.5f, .5f, .5f, .5f);
		
		to[4] = new track("Objects/Billboard.obj", "billboard",   CommonsJS.obj_Appearance(CommonsJS.Black), 0f, 3.8f, -8f, 4f,4f,4f);
		TransformGroup billboardTG = new TransformGroup();
		billboardTG.addChild(to[4].position_Object());
		billboardTG.setCollidable(false);
		
		to[5] = new track("Objects/checkpoint.obj", "start/finish", CommonsJS.obj_Appearance(CommonsJS.Red), 0f, 0f, 0.5f, .5f, .5f, .5f);
		
		carMovingPS = pointSound("sounds/movingCar.wav",carMovingPS);
		carIdlePS = pointSound("sounds/carInit.wav", carIdlePS);
		crashPS = pointSound("sounds/crash.wav",crashPS);
		
		cd = new CollisionDetect(carTG);
		cd.setSchedulingBounds(CommonsJS.twentyBS); 
		
		sceneTG.addChild(cd);
		sceneTG.addChild(holdCarTG);
		sceneTG.addChild(to[1].position_Object());
		sceneTG.addChild(billboardTG);
		
		//adds obstacles to the scene
		sceneTG.addChild(createObstacle(new Vector3d(5.0 ,0.0 ,-5.0),to[2]));	//creates an obstacle at position 5,0,-5
		sceneTG.addChild(createObstacle(new Vector3d(10.0 ,0.0 ,-1.0),to[2]));
		sceneTG.addChild(createObstacle(new Vector3d(10.0, 0.0 ,4.0),to[2]));
		sceneTG.addChild(createObstacle(new Vector3d(7.0, 0.0, 10.0),to[2]));
		sceneTG.addChild(createObstacle(new Vector3d(5.5, 0.0, 9.0),to[2]));
		sceneTG.addChild(createObstacle(new Vector3d(1.0, 0.0, 8.5),to[2]));
		sceneTG.addChild(createObstacle(new Vector3d(-2.0, 0.0, 8.0),to[2]));
		sceneTG.addChild(createObstacle(new Vector3d(-6.0, 0.0, 10.0),to[2]));
		sceneTG.addChild(createObstacle(new Vector3d(-8.0, 0.0, 6.0),to[2]));
		sceneTG.addChild(createObstacle(new Vector3d(-10.0, 0.0, 3.0),to[2]));
		sceneTG.addChild(createObstacle(new Vector3d(-7.0, 0.0, -2.0),to[2]));
		sceneTG.addChild(createObstacle(new Vector3d(-4.0, 0.0, -3.0),to[2]));
		
		//adds checkpoints
		sceneTG.addChild(createCheckpoint(new Vector3d(9.7, 0.0, 3.2),to[3],"check1"));
		sceneTG.addChild(createCheckpoint(new Vector3d(-0.5, 0.0, 8.9),to[3],"check2"));
		sceneTG.addChild(createCheckpoint(new Vector3d(-9.65, 0.0, 1.5),to[3],"check3"));
		sceneTG.addChild(createCheckpoint(new Vector3d(-4.0, 0.0, -4.0),to[3],"check4"));
		//starting/finish checkpoint
		sceneTG.addChild(createCheckpoint(new Vector3d(1.0, 0.0, -4.0),to[5],"start/finish"));
		
		//add text2D
		sceneTG.addChild(createText2D());
		
		//adds a cyan light so it's easier to see checkpoint3
		DirectionalLight light = new DirectionalLight(CommonsJS.Cyan, new Vector3f(-9.65f, 0.0f, 1.5f));
        light.setInfluencingBounds(new BoundingSphere(new Point3d(), 10.0));
        sceneTG.addChild(light);
		
		sceneTG.addChild(carMovingPS);
		sceneTG.addChild(carIdlePS);
		sceneTG.addChild(crashPS);				//not being used at the moment
		carMovingPS.setEnable(false);			//car is not moving at the start
		crashPS.setEnable(false);				//should only play when the car hits an object
		 
		carShape3D = to[0].get_Shape_3D();		//carShape3D points to the s3D for the car so the colour can change
		carShape3D.setCapability((Shape3D.ALLOW_APPEARANCE_WRITE));	
		
		
		objBG.addChild(CommonsJS.add_Lights(CommonsJS.White, 1000));
		
		objBG.addChild(sceneTG);
		
		//allows for picking in objBG
		pickTool = new PickTool(objBG);
		pickTool.setMode(PickTool.GEOMETRY);  
		return objBG;
	}
	
	//creates the text that displays when a checkpoint is hit
	private static BranchGroup createText2D() {

		BranchGroup objRoot = new BranchGroup();

		TransformGroup tg = new TransformGroup();
		Transform3D t3d = new Transform3D();
		tg.setCollidable(false);

		TransformGroup tg_2 = new TransformGroup();
		Transform3D t3d_2 = new Transform3D();
		tg_2.setCollidable(false);

		t3d.setTranslation(new Vector3d(-2.8, 5.2, -8.1));
		t3d.setRotation(new AxisAngle4f(0.0f, 0.0f, 0.0f, 0.0f));
		t3d.setScale(10);
		tg.setTransform(t3d);

		t3d_2.setTranslation(new Vector3d(-2, 4.5, -8.1));
		t3d_2.setRotation(new AxisAngle4f(0.0f, 0.0f, 0.0f, 0.0f));
		t3d_2.setScale(5);
		tg_2.setTransform(t3d_2);

		text2d = new Text2D("", new Color3f(1.0f, 0.0f, 0.0f), "Helvetica", 24, Font.NAME_FAMILY);
		text2d_2 = new Text2D("", new Color3f(1.0f, 0.0f, 0.0f), "Helvetica", 28, Font.NAME_FAMILY);
		
		text2d.setString("2800 Project");
		text2d_2.setString("Lap Time: " );
		
		tg.addChild(text2d);
		tg_2.addChild(text2d_2);
		objRoot.addChild(tg);
		objRoot.addChild(tg_2);

		objRoot.compile();

		return objRoot;

	}
	
	//creates an obstacle an positions it in the scene. Collision occurs when the car hits the box not the pylon
	public static TransformGroup createObstacle(Vector3d vector, TrackObjects to) {
		Transform3D moveObstacle3D = new Transform3D();
		moveObstacle3D.setTranslation(vector);		//moves both the obstacle and the box
		Transform3D obstacleBox3D = new Transform3D();
		TransformGroup obstacleTG = new TransformGroup(moveObstacle3D);
		TransformGroup obstacleBoxTG = new TransformGroup(obstacleBox3D);
		TransformGroup obstacleObjectTG = new TransformGroup();
		
		obstacleObjectTG.addChild(to.position_Object());
		obstacleObjectTG.setCollidable(false);    //collision can't happen with obstacle.obj, only the box. This prevents lag
		Shape3D box = new Box(0.1f, 0.1f, 0.1f); //box that collision will happen with
		box.setUserData("box");
		obstacleBoxTG.addChild(box);
		obstacleBox3D.setTranslation(new Vector3d(0.0, 0.2,0.0));  //move box inside the pylon so it can't be seen
		obstacleBoxTG.setTransform(obstacleBox3D);
		
		obstacleBoxTG.addChild(obstacleObjectTG);//the obstacleObjectTG is a child of the box TG because the obstacleObjectTG has no collision
		obstacleTG.addChild(obstacleBoxTG);
		
		return obstacleTG;
	}
	
	public static TransformGroup createCheckpoint(Vector3d vector, TrackObjects to, String checkpointStr) {
		Transform3D moveCheckpoint3D = new Transform3D();
		Transform3D checkpointBox3D = new Transform3D();
		TransformGroup checkpointTG = new TransformGroup(moveCheckpoint3D);
		TransformGroup checkpointBoxTG = new TransformGroup(checkpointBox3D);
		TransformGroup checkpointObjectTG = new TransformGroup();
		
		checkpointObjectTG.addChild(to.position_Object());
		checkpointObjectTG.setCollidable(false);    //collision can't happen with checkpoint.obj, only the box. This prevents lag

		Shape3D box = new Box(0.2f, 0.3f, 0.2f); //box that collision will happen with
		box.setAppearance(CommonsJS.obj_Appearance(CommonsJS.White));	//the box blends in with the white lines of the road
		
		box.setUserData(checkpointStr);
		checkpointBoxTG.addChild(box);
		
		checkpointBoxTG.addChild(checkpointObjectTG);//the obstacleObjectTG is a child of the box TG because the obstacleObjectTG has no collision
		checkpointTG.addChild(checkpointBoxTG);
		
		//rotates the checkpoints so they're facing the right direction
		Transform3D rotateCheckpointX3D = new Transform3D();
		Transform3D rotateCheckpointZ3D = new Transform3D();
		rotateCheckpointZ3D.rotZ(Math.PI/1.5);
		rotateCheckpointX3D.rotX(Math.PI*1.5);
		rotateCheckpointX3D.mul(rotateCheckpointZ3D);
		rotateCheckpointX3D.setTranslation(vector);
		TransformGroup rotateCheckPointTG = new TransformGroup(rotateCheckpointX3D);
		rotateCheckPointTG.addChild(checkpointTG);
		
		return rotateCheckPointTG;
	}

	
	
	public static class CollisionDetect extends Behavior {
		private boolean inCollision = false;
		private WakeupOnCollisionEntry wEnter;
		private WakeupOnCollisionExit wExit;
		TransformGroup tg;
		private Node theLeaf;

		public CollisionDetect(TransformGroup tg) {
			this.tg = tg;
			inCollision = false;
		}

		public void initialize() { 
			wEnter = new WakeupOnCollisionEntry(tg, WakeupOnCollisionEntry.USE_GEOMETRY);
			wExit = new WakeupOnCollisionExit(tg, WakeupOnCollisionExit.USE_GEOMETRY);
			wakeupOn(wEnter); // initialize the behavior
		}

		public void processStimulus(Iterator<WakeupCriterion> criteria) {
			WakeupCriterion theCriterion = (WakeupCriterion) criteria.next();
			boolean hitObstacle = false;
			
			if (theCriterion instanceof WakeupOnCollisionEntry) {
				theLeaf = ((WakeupOnCollisionEntry) theCriterion).getTriggeringPath().getObject();

			} else if (theCriterion instanceof WakeupOnCollisionExit) {
				theLeaf = ((WakeupOnCollisionExit) theCriterion).getTriggeringPath().getObject();

			} else {
				theLeaf = ((WakeupOnCollisionMovement) theCriterion).getTriggeringPath().getObject();

			}
			
			
			inCollision = !inCollision; // collision has taken place
			System.out.println("collision has occurred");
			if((String)theLeaf.getUserData() == "box") {
				System.out.println("hit box");
				//getSound("sounds/crash.wav"); // car crash sound effect
				hitObstacle = true;
			}
			
			if((String)theLeaf.getUserData() == "check1" || (String)theLeaf.getUserData() == "check2"
					|| (String)theLeaf.getUserData() == "check3" || (String)theLeaf.getUserData() == "check4" 
					|| (String)theLeaf.getUserData() == "start/finish") {
				System.out.println("hit checkpoint");
				//getSound("sounds/checkpt.wav"); // checkpoint sound effect
				
				if((String)theLeaf.getUserData() == "start/finish") {
					System.out.println("hit start/finish checkpoint");
					
					if(isRaceStarted == false) {
						isRaceStarted = true;
						text2d.setString("race started");
					}
					
					else if(isRaceStarted == true && isCheck4Hit) {
						isRaceStarted = false;
						isCheck4Hit = false;
						text2d.setString("Lap complete");
						startTime = System.currentTimeMillis();	
					}
				}
				
				else if((String)theLeaf.getUserData() == "check1"){
					isCheck1Hit = true;
					isRaceStarted = true;
					System.out.println("hit checkpoint1");
					text2d.setString("checkpoint 1");
					}
				else if((String)theLeaf.getUserData() == "check2" && isCheck1Hit){
					isCheck2Hit = true;
					System.out.println("hit checkpoint2");
					text2d.setString("checkpoint 2");
				}
				else if((String)theLeaf.getUserData() == "check3" && isCheck2Hit){
					isCheck3Hit = true;
					System.out.println("hit checkpoint3");
					text2d.setString("checkpoint 3");
				}
				else if((String)theLeaf.getUserData() == "check4" && isCheck3Hit){
					//resets checkpoints
					isCheck1Hit = false;
					isCheck2Hit = false;
					isCheck3Hit = false;
					isCheck4Hit = true;
					
					System.out.println("hit checkpoint4");
					text2d.setString("checkpoint 4");
				}
			}
			
			if (inCollision && hitObstacle) { 
				//getSound("sounds/crash.wav"); // car crash sound effect
				t3dstep.setTranslation(new Vector3d(-3d, 0.0,0.0));
				carTG.getTransform(car3D);
				car3D.mul(t3dstep);
				carTG.setTransform(car3D);
				
				wakeupOn(wExit); 
			} 
			else {
				wakeupOn(wEnter); // wait for collision happens
			}
		}
		
		//for collision sounds
		public void getSound(String fname) {
			Clip clip = null;

			try {
				URL url = this.getClass().getClassLoader().getResource(fname);
				AudioInputStream ai = AudioSystem.getAudioInputStream(url);
				clip = AudioSystem.getClip();
				clip.open(ai);
				clip.start();
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Texture texturedApp(String name) {
		String filename = name;
		TextureLoader loader = new TextureLoader(filename, null);
		ImageComponent2D image = loader.getImage();
		if (image == null)
		System.out.println("Cannot load file: " + filename);
		Texture2D texture = new Texture2D(Texture.BASE_LEVEL,
		Texture.RGBA, image.getWidth(), image.getHeight());
		texture.setImage(0, image);
		return texture;
	}

	
	private static Background createBkground(Color3f clr, BoundingSphere bounds) {
		Background bg = new Background();
		bg.setImage(new TextureLoader("images/racebk.jpg",null).getImage());
		bg.setImageScaleMode(Background.SCALE_FIT_MAX);
		bg.setApplicationBounds(bounds);
		bg.setColor(clr);
		return bg;
	}
	

	/* NOTE: Keep the constructor for each of the labs and assignments */
	public RaceGame(BranchGroup sceneBG) {
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		canvas = new Canvas3D(config);
		
		canvas.addKeyListener(this);
		canvas.addMouseListener(this);
		
		su = new SimpleUniverse(canvas);    // create a SimpleUniverse
		CommonsJS.define_Viewer(su, new Point3d(0.0d,9d, 33.0d));
		
		sceneBG.addChild(CommonsJS.key_Navigation(su));     // allow key navigation
		sceneBG.compile();		                           // optimize the BranchGroup
		su.addBranchGraph(sceneBG);  // attach the scene to SimpleUniverse
		enableAudio(su);

		setLayout(new BorderLayout());
		add("Center", canvas);
		frame.setSize(1000, 1000);                           // set the size of the JFrame
		frame.setVisible(true);
		
	}

	public static void main(String[] args) {
		frame = new JFrame("RaceGame");                   // NOTE: change JS to student's initials
		frame.getContentPane().add(new RaceGame(Shapes()));  // create an instance of the class
		startAnimation();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	//When the User clicks the mouse
	public void mouseClicked(MouseEvent event) {
		int x = event.getX(); int y = event.getY();     // mouse coordinates
		Point3d point3d = new Point3d(), center = new Point3d();
		//System.out.println("X: " + x + "\tY: " + y);
		
		canvas.getPixelLocationInImagePlate(x, y, point3d);// obtain AWT pixel in ImagePlate coordinates
		canvas.getCenterEyeInImagePlate(center);           // obtain eye's position in IP coordinates
		
		Transform3D transform3D = new Transform3D();       // matrix to relate ImagePlate coordinates~
		canvas.getImagePlateToVworld(transform3D);         // to Virtual World coordinates
		transform3D.transform(point3d);                    // transform 'point3d' with 'transform3D'
		transform3D.transform(center);                     // transform 'center' with 'transform3D'

		Vector3d mouseVec = new Vector3d();
		mouseVec.sub(point3d, center);
		mouseVec.normalize();
		pickTool.setShapeRay(point3d, mouseVec);
		
		PickResult result = pickTool.pickClosest();
		if(result != null) {
			Node node = result.getNode(PickResult.SHAPE3D);
			String userData = (String) node.getUserData();
			System.out.println(userData + " was clicked");
			Appearance app = new Appearance();
			
			if(userData == "car") { 		//if the car is clicked on
				app =  CommonsJS.obj_Appearance(CommonsJS.clr_list[carColourItterator]);
				carColourItterator++; 		//access the next element of the clr_list for when the car is clicked again
				
				//this makes sure CommonsJS.clr_list[carColourItterator] dosen't go out of bounds
				if(carColourItterator == 7) {
					carColourItterator = 0;
				}
				carShape3D.setAppearance(app);
			}
		}
	}
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }
	public void mousePressed(MouseEvent e) { }
	public void mouseReleased(MouseEvent e) { }

	//when the user presses a button
	public void keyTyped(KeyEvent e) {
		
		//this if statement makes the carMovingSound start only when a key is pressed.
		
		Character key = e.getKeyChar();
		key = Character.toLowerCase(key);
		pressedKeys.add(e.getKeyCode());
		
		long currentTime = System.currentTimeMillis();
		long time = currentTime - startTime - lapTime;
		time /= 1000;
		text2d_2.setString("Lap time: " + time);		//updates time on billboard
		
		if(key == 'w' || key == 's' || key == 'd' || key == 'a') {
			carMovingPS.setMute(false);		//car is now moving and will make sound
			carIdlePS.setMute(true);
			
			if(isFirstKeyPressed == false) {
				carMovingPS.setEnable(true);
			}
			isFirstKeyPressed = true;
		}
		
		//Changes the POV of the camera
		if(key == '1'){
			CommonsJS.define_Viewer(su, new Point3d(0.0d, 17.0d, 32.0d));
			isFollow = false;
		}
		
		if(key == '2'){
			CommonsJS.define_Viewer(su, new Point3d(0.0d, 35.0d, 5.0d));
			isFollow = false;
		}
		
		if(key == '3'){
			CommonsJS.define_Viewer(su, new Point3d(30.0d, 30.0d, 0.0d));
			isFollow = false;
		}
		
		if(key == '4') {
			CommonsJS.define_Viewer(su, new Point3d(0.0d,9d, 33.0d));
			isFollow = false;
		}
		if(key == '5') { // will be set to true even if current key pressed is not 5 ex. 'w'
			isFollow = true;
			
		}
		
    }
	
	public void keyPressed(KeyEvent e) {
		pressedKeys.add(e.getKeyCode());
		
		if (!pressedKeys.contains(KeyEvent.VK_S)) {			// 'd' turns right, 'a' turns left (if going forward, or spinning on itself)
			if (pressedKeys.contains(KeyEvent.VK_W))
				endPos.x = carX;
			if (pressedKeys.contains(KeyEvent.VK_A)) {
				right = false;								
				isRotation = true;							// rotation enabled in the update() method
			}
			if (pressedKeys.contains(KeyEvent.VK_D)) {
				right = true;
				isRotation = true;
			}
		} else {											// 'd' goes backwards & left, 'a' goes backwards & right
			if (pressedKeys.contains(KeyEvent.VK_S))
				endPos.x = -carX;
			if (pressedKeys.contains(KeyEvent.VK_A)){
				right = true;
				isRotation = true;
			}
			if (pressedKeys.contains(KeyEvent.VK_D)) {
				right = false;
				isRotation = true;
			}
		}
	}
	
	public void keyReleased(KeyEvent e) {
		Character key = e.getKeyChar();
		key = key.toLowerCase(key);
		
		if (key == 'd' || key == 'a') {
			isRotation = false;				// rotation disabled in update method	
			
			// sound
			carIdlePS.setMute(false);			
			carMovingPS.setMute(true);		

		} else if (key == 'w' || key == 's'){
			// sound
			carIdlePS.setMute(false);			
			carMovingPS.setMute(true);	
			
			// stop movement
			endPos.x = 0f;
		}
		pressedKeys.remove(e.getKeyCode());
	}
	
	private void enableAudio(SimpleUniverse simple_U) {

		JOALMixer mixer = null;		                         // create a null mixer as a joalmixer
		Viewer viewer = simple_U.getViewer();
		viewer.getView().setBackClipDistance(20.0f);         // make object(s) disappear beyond 20f 

		if (mixer == null && viewer.getView().getUserHeadToVworldEnable()) {			                                                 
			mixer = new JOALMixer(viewer.getPhysicalEnvironment());
			if (!mixer.initialize()) {                       // add mixer as audio device if successful
				System.out.println("Open AL failed to init");
				viewer.getPhysicalEnvironment().setAudioDevice(null);
			}
		}
	}
	
	/* a function to create a PointSound at the origin of its reference frame */
	private static PointSound pointSound(String filePath, PointSound ps) {
		URL url = null;
		String filename = filePath;
		try {
			url = new URL("file", "localhost", filename);
		} catch (Exception e) {
			System.out.println("Can't open " + filename);
		} 
		ps.setCapability(PointSound.ALLOW_POSITION_WRITE);
		ps.setCapability(PointSound.ALLOW_ENABLE_WRITE);
		ps.setCapability(PointSound.ALLOW_PAUSE_WRITE);
		ps.setCapability(PointSound.ALLOW_MUTE_WRITE);
		// create and position a point sound
		PointSoundBehavior player = new PointSoundBehavior(ps, url, new Point3f(0.0f, 0.0f, 0.0f));
		player.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0));
		return ps;
	}
	
	public static void rotateCar(double angle) {
		carTG.getTransform(car3D);
		
		Transform3D rotation = new Transform3D();
		rotation.rotY(angle);
		car3D.mul(rotation);
		
		carTG.setTransform(car3D);
	}
	
	public static boolean isFrameOpen(JFrame frame) {
	    if (frame != null && frame.isVisible()) {
	        return true;
	    }
	    return false;
	}
	
	public static void update(long elapsedTime) {
	    // calculate the normalized time elapsed
	    float t = (float) elapsedTime / 1000f;
	    
	    // interpolate the current position based on the elapsed time
	    startPos.interpolate(startPos, endPos, t);
	    
	    // if rotation enabled
	    if(isRotation) {
	    	if(right)
	    		rotateCar(-Math.PI / 80);
	    	else
	    		rotateCar(Math.PI / 80);
	    }
	    
	    if(isFollow) {
	    	viewFollowCar();
	    }
	    
	    // update the object's transform
	    t3dstep.setTranslation(startPos);
	    carTG.getTransform(car3D);
		car3D.mul(t3dstep);
		carTG.setTransform(car3D);
	}
	
	public static void startAnimation() {
		long lastTime = System.currentTimeMillis();
	    boolean running = true;
	    while (running) {
	        long currentTime = System.currentTimeMillis();
	        long elapsedTime = currentTime - lastTime;
	        lastTime = currentTime;

	        // update the animation based on the elapsed time
	        update(elapsedTime);

	        // pause for a short time to avoid using too much CPU
	        try {
	            Thread.sleep(10);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }

	        // check if the panel is still open (good practice)
	        running = isFrameOpen(frame);
	    }
	}
	
	public static void viewFollowCar () {
		    Transform3D viewTransform = new Transform3D();
		    viewTransform.setIdentity();

		    // Get the current position and orientation of the object
		    Transform3D t3d = new Transform3D();
		    carTG.getTransform(t3d);
		    Vector3d ct = new Vector3d();
		    Matrix3d cr = new Matrix3d();
		    t3d.get(ct);
			t3d.getRotationScale(cr);
		    ct.setY(0.3);				// adjustements because car is actually located at Y(0.3) & Z(-4)(origin)
		    ct.setZ(ct.getZ()-4);		
		    
		    viewTransform.setRotation(cr);
		    viewTransform.setTranslation(ct);
		    
		    // lookAt method needs: center, up, eye
		    // - > center & up
		    Point3d center = new Point3d(ct);
		    Vector3d up = new Vector3d(0.0, 1.0, 0.0);
		    
		   	// - > eye...
		    // get the angle of the car's position (rotY)
		    Transform3D angle3d = new Transform3D(car3D);
		    Matrix3d rot = new Matrix3d();
		    angle3d.get(rot);
		    
		    // angle
		    double radius = 10;
		    double angle = Math.atan2(rot.m20, rot.m22);
		    double x = center.x + radius * Math.cos(angle) * -1;
		    double z = center.z + radius * Math.sin(angle) * -1;
		    
		    // position
		    Point3d eye = new Point3d(x, ct.y + 1.5, z);
		    
		    // Transform3D lookAt method
		    viewTransform.lookAt(eye, center, up);

		    // Invert the transform and applied to the view transform group
		    viewTransform.invert();
		    TransformGroup viewTG = su.getViewingPlatform().getViewPlatformTransform();
		    viewTG.setTransform(viewTransform);

	}

}
