package cubyz.gui.components;

import java.util.ArrayList;

import org.joml.Vector4i;

import cubyz.rendering.Graphics;
import cubyz.rendering.Window;

/**
 * A Component that contains other Components.
 */

public abstract class Container extends Component {

	protected ArrayList<Component> childrens;
	
	public Container() {
		childrens = new ArrayList<>();
	}
	
	public void add(Component comp) {
		if (comp == this) throw new IllegalArgumentException("comp == this");
		childrens.add(comp);
	}
	
	public void remove(Component comp) {
		childrens.remove(comp);
	}
	
	public void remove(int index) {
		childrens.remove(index);
	}

	@Override
	public void render(int x, int y) {
		Vector4i oldClip = Graphics.setClip(new Vector4i(x, Window.getHeight() - y - height, width, height));
		for (Component child : childrens) {
			child.renderInContainer(x, y, width, height);
		}
		Graphics.restoreClip(oldClip);
	}
	
}
