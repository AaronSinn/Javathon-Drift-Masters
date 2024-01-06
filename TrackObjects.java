package CodesAS2800;

import java.awt.Font;
import java.io.FileNotFoundException;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Font3D;
import org.jogamp.java3d.FontExtrusion;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Text3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.loaders.IncorrectFormatException;
import org.jogamp.java3d.loaders.ParsingErrorException;
import org.jogamp.java3d.loaders.Scene;
import org.jogamp.java3d.loaders.objectfile.ObjectFile;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

public abstract class TrackObjects {
	protected abstract Node create_Object();	           // use 'Node' for both Group and Shape3D
	public abstract Node position_Object();
	
	
	public Shape3D s3D;
	public Shape3D get_Shape_3D() {						  //returns an objects Shape3D so we can modify it
		return s3D;
	}
}

class track extends TrackObjects {

	private TransformGroup objTG;                          // use 'objTG' to position an object
	String s, userData;
	Appearance ap;
	float x, y, z; //translation 
	float a, b, c; //scaling
	public track(String st, String userData, Appearance app, float tx, float ty, float tz, float sx, float sy, float sz) {
		s = st; //object file location
		ap = app; //color
		x = tx; //translations: x, y, z
		y = ty;
		z = tz;
		a = sx; //scalings: x, y, z
		b = sy;
		c = sz;
		this.userData = userData;    //used to tell which object is clicked on
	}
	protected Node create_Object() {
		int flags1 = ObjectFile.RESIZE | ObjectFile.TRIANGULATE | ObjectFile.STRIPIFY;
		ObjectFile f1 = new ObjectFile(flags1, (float) (60 * Math.PI / 180.0));
		Scene s1 = null;
		try {
		s1 = f1.load(s);
		System.out.println(userData + " loaded");
		} catch (FileNotFoundException e) {
		System.err.println(e);
		System.exit(1);
		} catch (ParsingErrorException e) {
		System.err.println(e);
		System.exit(1);
		} catch (IncorrectFormatException e) {
		System.err.println(e);
		System.exit(1);
		}
		
		BranchGroup bg = s1.getSceneGroup();
		s3D = (Shape3D) bg.getChild(0);
		s3D.setAppearance(ap);
		s3D.setUserData(userData);
		Transform3D tr = new Transform3D();
		tr.setTranslation(new Vector3f(x, y, z));
		tr.setScale(new Vector3d(a, b, c));
		
		TransformGroup tg = new TransformGroup(tr);
		tg.addChild(bg);
		return tg;
	}


	public Node position_Object() {
		return create_Object();
	}
	
}
