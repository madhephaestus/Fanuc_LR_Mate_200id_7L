
import com.neuronrobotics.bowlerstudio.physics.TransformFactory
import com.neuronrobotics.sdk.addons.kinematics.DHChain;
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import java.util.ArrayList;

import com.neuronrobotics.sdk.addons.kinematics.DHChain;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DhInverseSolver;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.Log;
import Jama.Matrix;
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Cylinder
import eu.mihosoft.vrl.v3d.Transform;
import javafx.application.Platform
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class scriptJavaIKModel implements DhInverseSolver {
	boolean debug = false;
	CSG blue =null;
	CSG green =null;
	CSG red =null;
	CSG white =null;
	int limbIndex =0;
	public scriptJavaIKModel(int index){
		limbIndex=index;
	}
	@Override
	public double[] inverseKinematics(TransformNR target, double[] jointSpaceVector, DHChain chain) {
		ArrayList<DHLink> links = chain.getLinks();
		if(links.size()==3 || (links.size()==4 && (Math.abs(links.get(2).alpha)<0.001)))
			return inverseKinematics34dof(target,jointSpaceVector,chain);
		return inverseKinematics6dof(target,jointSpaceVector,chain);
	}
	TransformNR linkOffset(DHLink link) {
		return new TransformNR(link.DhStep(0))
	}
	public double[] inverseKinematics6dof(TransformNR target, double[] jointSpaceVector, DHChain chain) {
//		if(debug) {
//			if(blue==null) {
//				blue=new Cylinder(0, 5, 30,9).toCSG()
//						//.roty(-90)
//						.setColor(javafx.scene.paint.Color.BLUE)
//				green=new Cylinder(0, 5, 30,9).toCSG()//.roty(-90)
//						.setColor(javafx.scene.paint.Color.GREEN)
//				red=new Cylinder(0, 5, 30,9).toCSG()//.roty(-90)
//				.setColor(javafx.scene.paint.Color.RED)
//				white=new Cylinder(0, 5, 30,9).toCSG()//.roty(-90)
//				
//						.setColor(javafx.scene.paint.Color.WHITE)
//			}
//			BowlerStudioController.addCsg(blue)
//			BowlerStudioController.addCsg(green)
//			BowlerStudioController.addCsg(red)
//			BowlerStudioController.addCsg(white)
//			if(debug)Platform.runLater({TransformFactory.nrToAffine(target,white.getManipulator())})
//		}
		//System.out.println("My 6dof IK "+target);
		ArrayList<DHLink> links = chain.getLinks();
		int linkNum = jointSpaceVector.length;
		TransformNR l0Offset = linkOffset(links.get(0))
		TransformNR l1Offset = linkOffset(links.get(1))
		TransformNR l2Offset = linkOffset(links.get(2))
		TransformNR l3Offset = linkOffset(links.get(3))
		// Vector decompose the tip target
		double z = target.getZ();
		double y = target.getY();
		double x = target.getX();
		def targetNoRot =new TransformNR(x,y,z,new RotationNR())
		
		RotationNR q = target.getRotation();
		def newCenter =target.copy()
		// Start by finding the IK to the wrist center
		if(linkNum>=6) {
			//offset for tool
			if(debug)println "Offestting for tool"
			def tool = new TransformNR()
			if(linkNum==7)
				tool=linkOffset(links.get(6))
			// compute the transform from tip to wrist center
			def wristCenterOffsetTransform = linkOffset(links.get(5)).times(tool)
			//println wristCenterOffsetTransform
			// take off the tool from the target to get the center of the wrist
			newCenter = target.times(wristCenterOffsetTransform.inverse())

			//if(debug)Platform.runLater({TransformFactory.nrToAffine(newCenter,tipPointer2.getManipulator())})
		}
		def virtualcenter = newCenter.times(new TransformNR(0,0,10,
			 new RotationNR(Math.toDegrees(links.get(5).getAlpha()),0,0)))
		// recompute the X,y,z with the new center
		z = newCenter.getZ();
		y = newCenter.getY();
		x = newCenter.getX();
		//xyz now are at the wrist center
		// Compute the xy plane projection of the tip
		// this is the angle of the tipto the base link
		if(x==0&&y==0) {
			println "Singularity! try something else"
			return inverseKinematics6dof(target.copy().translateX(0.01));
		}

		double baseVectorAngle = Math.atan2(y , x);
		double a1d = Math.toDegrees(baseVectorAngle);
		// this projection number becomes the base link angle directly
		jointSpaceVector[0]=a1d;
		if(debug)println "New base "+a1d
		//jointSpaceVector[0]=0;// TESTING

		// Rotate the tip into the xZ plane
		// apply a transform to the tip here to compute where it
		// would be on the ZX plane if the base angel was 0

		def firstLink =new TransformNR(links.get(0).DhStep(baseVectorAngle)).inverse()
		def tipNoRot =new TransformNR(x,y,z,new RotationNR())

		//println "Incomming tip target Tip \tx="+x+" z="+z+" and y="+y+" alph baseLink "+alphaBase
		//println firstLink
		//println tipNoRot

		def newTip = firstLink
				.times(tipNoRot)

		x=newTip.getX()
		y=newTip.getY()
		z=newTip.getZ()
		if(x==0&&y==0) {
			println "Singularity! try something else"
			return inverseKinematics6dof(target.copy().translateX(0.01));
		}
		if(debug)println "New Tip                             \tx="+x+" y="+y+" and z should be 0 and is="+z


		//println newTip
		// Tip y should be 0
		// this is the angle of the vector from base to tip
		double tipToBaseAngle = Math.atan2(y,x); // we now have the rest of the links in the XY plane
		
		double tipToBaseAngleDegrees = Math.toDegrees(tipToBaseAngle);
		if(debug)println "Base link to tip angle elevation "+tipToBaseAngleDegrees
		def transformAngleOfTipToTriangle = new TransformNR(0,0,0,new RotationNR(0,-tipToBaseAngleDegrees,0))
		def xyTip = new TransformNR(x,y,0,new RotationNR())
		//Transform the tip into the x Vector
		def tipXPlane =transformAngleOfTipToTriangle
				.times(xyTip)
		//println tipXYPlane
		double wristVect = tipXPlane.getX();
		// add together the last two links
		TransformNR wristCenterToElbow = 	l2Offset.times(l3Offset)//.inverse()
		// find the angle formed by the two links, includes the elbows theta
		if(wristCenterToElbow.getX()==0&&wristCenterToElbow.getY()==0) {
			println "Singularity! try something else"
			return inverseKinematics6dof(target.copy().translateX(0.01));
		}
		double elbowLink2CompositeAngle = Math.atan2(wristCenterToElbow.getY(),wristCenterToElbow.getX());
		double elbowLink2CompositeAngleDegrees = Math.toDegrees(elbowLink2CompositeAngle)
		// COmpute teh vector length of the two links combined
		double elbowLink2CompositeLength = Math.sqrt(
				Math.pow(wristCenterToElbow.getY(), 2) +
				Math.pow(wristCenterToElbow.getX(), 2));
		// assume no D on this link as that would break everything
		double elbowLink1CompositeLength = links.get(1).getR();
		if(debug)println "Elbow 2 link data "+elbowLink2CompositeAngleDegrees+" vector "+elbowLink2CompositeLength

		if(wristVect>elbowLink2CompositeLength+elbowLink1CompositeLength)
			throw new ArithmeticException("Total reach longer than possible "+target);
		// Use the law of cosines to calculate the elbow and the shoulder tilt
		double shoulderTiltAngle =-( Math.toDegrees(Math.acos(
				(Math.pow(elbowLink1CompositeLength,2)+Math.pow(wristVect,2)-Math.pow(elbowLink2CompositeLength,2))/
				(2*elbowLink1CompositeLength*wristVect)
				))-tipToBaseAngleDegrees+Math.toDegrees(links.get(1).getTheta()))
		double elbowTiltAngle =-( Math.toDegrees(Math.acos(
				(Math.pow(elbowLink2CompositeLength,2)+Math.pow(elbowLink1CompositeLength,2)-Math.pow(wristVect,2))/
				(2*elbowLink2CompositeLength*elbowLink1CompositeLength)
				))+elbowLink2CompositeAngleDegrees-180)
		jointSpaceVector[2]=elbowTiltAngle
		jointSpaceVector[1]=shoulderTiltAngle
		
		
		/**
		// compute the top of the wrist now that the first 3 links are calculated
		 * 
		 */
		double[] wristLinks=new double[jointSpaceVector.length]
		for(int i=0;i<3;i++) {
			wristLinks[i]=jointSpaceVector[i];
		}
		for(int i=3;i<jointSpaceVector.length;i++) {
			wristLinks[i]=0
		}
		ArrayList<TransformNR> chainToLoad =[]
		chain.forwardKinematicsMatrix(wristLinks,chainToLoad)
		def	startOfWristSet=chain.kin.inverseOffset(chainToLoad.get(2));
		TransformNR wristMOvedToCenter0 =startOfWristSet
											.inverse()// move back from base ot wrist to world home
											.times(virtualcenter)// move forward to target, leaving the angle between the tip and the start of the rotation 
		if(debug)println 	wristMOvedToCenter0								
		RotationNR qWrist=wristMOvedToCenter0.getRotation()
		if(wristMOvedToCenter0.getX()==0&&wristMOvedToCenter0.getY()==0) {
			println "Singularity! try something else"
			return inverseKinematics6dof(target.copy().translateX(0.01));
		}
		def closest= (Math.toDegrees(Math.atan2(wristMOvedToCenter0.getY(), wristMOvedToCenter0.getX()))-Math.toDegrees(links.get(3).getTheta()))
//		def options = [ closest,closest+180,closest-180]
//		def currentWristStart=jointSpaceVector[3]
//		for(def val:options) {
//			def delt =  Math.abs(currentWristStart-val)
//			if(delt<Math.abs(currentWristStart-closest)) {
//				closest=val
//			}
//		}
		jointSpaceVector[3]=closest
		wristLinks[3]=jointSpaceVector[3]
		if(jointSpaceVector.length==4)
			return jointSpaceVector
		
		chainToLoad =[]
		/**
		// Calculte the second angle
		 * 
		 */
		chainToLoad.clear()
		chain.forwardKinematicsMatrix(wristLinks,chainToLoad)
		def	startOfWristSet2=chain.kin.inverseOffset(chainToLoad.get(3));
		TransformNR wristMOvedToCenter1 =startOfWristSet2
											.inverse()// move back from base ot wrist to world home
											.times(virtualcenter)// move forward to target, leaving the angle between the tip and the start of the rotation
		if(debug)println " Middle link ="	+wristMOvedToCenter1
		RotationNR qWrist2=wristMOvedToCenter1.getRotation()
		if(wristMOvedToCenter1.getX()==0&&wristMOvedToCenter1.getY()==0) {
			println "Singularity! try something else"
			return inverseKinematics6dof(target.copy().translateX(0.01));
		}
		jointSpaceVector[4]=(Math.toDegrees(Math.atan2(wristMOvedToCenter1.getY(), wristMOvedToCenter1.getX()))-
			Math.toDegrees(links.get(4).getTheta())-
			90)
		wristLinks[4]=jointSpaceVector[4]
		if(jointSpaceVector.length==5)
			return jointSpaceVector
		chainToLoad =[]
		/**
		// Calculte the last angle
		 * 
		 */
		chain.forwardKinematicsMatrix(wristLinks,chainToLoad)
		def	startOfWristSet3=chain.kin.inverseOffset(chainToLoad.get(4));
		def tool = new TransformNR()
		if(linkNum==7)
			tool=linkOffset(links.get(6))
		TransformNR wristMOvedToCenter2 =startOfWristSet3
											.inverse()// move back from base ot wrist to world home
											.times(target.times(tool.inverse()))// move forward to target, leaving the angle between the tip and the start of the rotation
		if(debug)println "\n\nLastLink "	+wristMOvedToCenter2
		RotationNR qWrist3=wristMOvedToCenter2.getRotation()
		jointSpaceVector[5]=(Math.toDegrees(qWrist3.getRotationAzimuth())-Math.toDegrees(links.get(5).getTheta()))
		
		if(debug)Platform.runLater({TransformFactory.nrToAffine(wristMOvedToCenter0,blue.getManipulator())})
		if(debug)Platform.runLater({TransformFactory.nrToAffine(wristMOvedToCenter1,green.getManipulator())})
		if(debug)Platform.runLater({TransformFactory.nrToAffine(wristMOvedToCenter2,red.getManipulator())})

		for(int i=3;i<jointSpaceVector.length;i++) {
			if(jointSpaceVector[i]>180)
				jointSpaceVector[i]-=360.0
			if(jointSpaceVector[i]<-180)
				jointSpaceVector[i]+=360.0
		}
//		for(int i=0;i<jointSpaceVector.length;i++) {
//			if(Math.abs(jointSpaceVector[i])<0.001) {
//				jointSpaceVector[i]=0;
//			}
//		}
		if(debug)println "Euler Decomposition proccesed \n"+jointSpaceVector[3]+" \n"+jointSpaceVector[4]+" \n"+jointSpaceVector[5]
		//println "Law of cosines results "+shoulderTiltAngle+" and "+elbowTiltAngle
		return jointSpaceVector;
	}

	public double[] inverseKinematics34dof(TransformNR target, double[] jointSpaceVector, DHChain chain) {
		//System.out.println("My IK");
		//		try {
		ArrayList<DHLink> links = chain.getLinks();
		int linkNum = jointSpaceVector.length;

		double z = target.getZ();
		double y = target.getY();
		double x = target.getX();
		//		  z = Math.round(z*100.0)/100.0;
		//		  y = Math.round(y*100.0)/100.0;
		//            x = Math.round(x*100.0)/100.0;
		//
		RotationNR q = target.getRotation();

		//System.out.println("elevation: " + elev);
		//System.out.println("z: " + z);
		//System.out.println("y: " + y);
		//System.out.println("x: " + x);

		//double Oang = Math.PI/2 + q.getRotationElevation();
		//            double Oang = Math.toRadians(45);
		//
		//            double Oanginv = (Math.PI/2) - Oang;

		double l1_d = links.get(0).getR();
		double l2_d = links.get(1).getR();
		double l3_d = links.get(2).getR();

		double l4_d=0;// in 3 dof, this is 0
		if(links.size()>3)
			l4_d   = links.get(3).getR();

		//System.out.println("L1: " + l1_d);
		//System.out.println("L2: " + l2_d);
		//System.out.println("L3: " + l3_d);
		//System.out.println("L4: " + l4_d);


		double[] inv = new double[linkNum];
		double a1 = Math.atan2(y , x);
		double a1d = Math.toDegrees(a1);

		def newTip = new Transform()
				.movex(x)
				.movey(y)
				.movez(z)
				.rotz(a1d)
		//println newTip

		x=newTip.getX()
		y=newTip.getY()
		z=newTip.getZ()
		//System.out.println(" Base Angle : " + a1d);
		//System.out.println(" Base Orented z: " + z);
		//System.out.println(" Base Orented y: " + y);
		//System.out.println(" Base Orented x: " + x);

		double a2 = Math.atan2(z,x); // Z angle using x axis and z axis
		double a2d = Math.toDegrees(a2);
		//println a2d
		double elev = Math.toDegrees(q.getRotationElevation() )
		//println "R Vector Angle "+a2d

		//		double r1 = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)); // X and Y plane Vector
		//		double r2 = Math.sqrt(Math.pow(x, 2) + Math.pow(y,2)+Math.pow(z, 2)); // Leg Vector
		//		double r3 = Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2)); // x and z vector
		/*
		 def rvector = new Cube(r2,1,1).toCSG()
		 .toXMin()
		 .roty(a2d)
		 def rvectorOrig = rvector.rotz(-a1d)
		 .setColor(javafx.scene.paint.Color.BLUE)
		 BowlerStudioController.addCsg(rvector)
		 BowlerStudioController.addCsg(rvectorOrig)
		 */
		def wristCenter = new Transform()
				.movex(-l4_d)
				.roty(-elev)
				.movey(y)
				.movez(z-links.get(0).getD())
				.movex(x-l1_d)
		/*
		 def foot = new Cube(l4_d>0?l4_d:1,1,1).toCSG()
		 .toXMin()
		 .transformed(wristCenter)
		 .setColor(javafx.scene.paint.Color.AQUA)
		 BowlerStudioController.addCsg(foot)
		 */
		x=wristCenter.getX()
		y=wristCenter.getY()
		z=wristCenter.getZ()
		double wristAngle = Math.atan2(z,x);
		double wristAngleDeg =Math.toDegrees(wristAngle)
		double wristVect =  Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2)); // x and z vector
		if(wristVect>l2_d+l3_d)
			throw new ArithmeticException("Total reach longer than possible "+inv);
		//System.out.println(" Wrist Angle: " + wristAngleDeg);
		//System.out.println(" Wrist Vect: " + wristVect);
		//System.out.println(" Wrist z: " + z);
		//System.out.println(" Wrist y: " + y);
		//System.out.println(" Wrist x: " + x);
		/*
		 def wristVector = new Cube(wristVect,1,1).toCSG()
		 .toXMin()
		 .roty(wristAngleDeg)
		 .setColor(javafx.scene.paint.Color.WHITE)
		 BowlerStudioController.addCsg(wristVector)
		 */
		double shoulderTiltAngle = Math.toDegrees(Math.acos(
				(Math.pow(l2_d,2)+Math.pow(wristVect,2)-Math.pow(l3_d,2))/
				(2*l2_d*wristVect)
				))
		double elbowTiltAngle = Math.toDegrees(Math.acos(
				(Math.pow(l3_d,2)+Math.pow(l2_d,2)-Math.pow(wristVect,2))/
				(2*l3_d*l2_d)
				))
		/*
		 def shoulderVector = new Cube(l2_d,1,1).toCSG()
		 .toXMin()
		 .roty(wristAngleDeg+shoulderTiltAngle)
		 .setColor(javafx.scene.paint.Color.GRAY)
		 BowlerStudioController.addCsg(shoulderVector)
		 */
		inv[0]=a1d
		if(Math.toDegrees(links.get(2).getTheta())<0){
			inv[1]=wristAngleDeg+shoulderTiltAngle-Math.toDegrees(links.get(1).getTheta())
			inv[2]=elbowTiltAngle-180-Math.toDegrees(links.get(2).getTheta())
		}else{
			inv[1]=-(wristAngleDeg+shoulderTiltAngle+Math.toDegrees(links.get(1).getTheta()))
			inv[2]=(180-elbowTiltAngle-Math.toDegrees(links.get(2).getTheta()))
		}
		if(links.size()>3)
			inv[3]=-inv[1]-inv[2]-Math.toDegrees(links.get(3).getTheta())-elev-
					Math.toDegrees(links.get(1).getTheta())-
					Math.toDegrees(links.get(2).getTheta())
		//System.out.println(inv[0]);
		//System.out.println(inv[1]);
		//System.out.println(inv[2]);
		//System.out.println(inv[3]);
		if(links.size()>3)
			if(Double.isNaN(inv[0]) || Double.isNaN(inv[1]) || Double.isNaN(inv[2]) || Double.isNaN(inv[3]))
				throw new ArithmeticException("Can't move to that position "+inv);
			else if(Double.isNaN(inv[0]) || Double.isNaN(inv[1]) || Double.isNaN(inv[2]) )
				throw new ArithmeticException("Can't move to that position "+inv);

		//println "Success "+inv
		return inv;
		//		} catch (Throwable t) {
		//			BowlerStudio.printStackTrace(t);
		//			return jointSpaceVector;
		//		}
	}

}

if(args==null)
	args=[0]
return new scriptJavaIKModel (args[0])