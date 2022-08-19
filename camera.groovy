//Your code here
import  eu.mihosoft.vrl.v3d.ext.quickhull3d.*
import eu.mihosoft.vrl.v3d.CSG
import eu.mihosoft.vrl.v3d.Cube
import eu.mihosoft.vrl.v3d.Cylinder
import eu.mihosoft.vrl.v3d.Vector3d

if(args==null)
	args=[1280,720.0,950]
double ratio = args[1]/args[0]
println ratio
double height = 10
double percentHeight = height/args[1]

def points = [	new Vector3d(args[1]*percentHeight/2, args[0]*percentHeight/2, 0),
			new Vector3d(args[1]*percentHeight/2, -args[0]*percentHeight/2, 0),
			new Vector3d(-args[1]*percentHeight/2, args[0]*percentHeight/2, 0),
			new Vector3d(-args[1]*percentHeight/2, -args[0]*percentHeight/2, 0),
			new Vector3d(0, 0, height)
]
CSG viewbox = HullUtil.hull(points)
				.toZMax()
CSG box = new Cube(height/1.5,height/4,height).toCSG()
			.toZMin()

CSG simpleSyntax =new Cylinder(height/2,height/4).toCSG()
					.rotx(90)
					.toXMin()
					.movex(height/4)
					.movey(-height/8)

viewbox=viewbox.union(box,simpleSyntax,simpleSyntax.movez(height))
return viewbox