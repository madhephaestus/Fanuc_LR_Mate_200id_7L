import java.util.ArrayList;

import com.neuronrobotics.sdk.addons.kinematics.AbstractLink;
import com.neuronrobotics.sdk.addons.kinematics.INewLinkProvider;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.LinkFactory;

public class MockCMOD implements INewLinkProvider{
	private static final double CLOSE_VLAUE = -3;

	private static final double OPEN_ANGLE = 6.5;

	ArrayList<AbstractLink> links = new ArrayList<>();
	
	private double angle = 0;

	public MockCMOD() {
		LinkFactory.addLinkProvider("CMOD", this);
	}

	public void setClose(boolean val) {
		angle=val?OPEN_ANGLE:CLOSE_VLAUE;
		println "Gripper "+val
		for(int i=0;i<links.size();i++) {
			links.get(i).fireLinkListener(angle);
		}
	}

	@Override
	public AbstractLink generate(LinkConfiguration lc) {
		
		AbstractLink abstractLink = new AbstractLink(lc) {
			
			@Override
			public double getCurrentPosition() {
				return angle;
			}
			
			@Override
			public void flushDevice(double v) {
				angle=getTargetValue();
			}
			
			@Override
			public void flushAllDevice(double v) {
				angle=getTargetValue();
			}
			
			@Override
			public void cacheTargetValueDevice() {
				
			}
		};
		abstractLink.setDeviceMaximumValue(OPEN_ANGLE);
		abstractLink.setDeviceMinimumValue(CLOSE_VLAUE);
		links.add(abstractLink);
		return abstractLink;
	}

}

return new MockCMOD()