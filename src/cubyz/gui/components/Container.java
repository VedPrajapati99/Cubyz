package cubyz.gui.components;

import java.util.ArrayList;

import cubyz.client.rendering.Window;
import cubyz.gui.Component;

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
	public void render(long nvg, Window src) {
		for (Component child : childrens) {
			child.render(nvg, src);
		}
	}
	
}