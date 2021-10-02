package cubyz.world.entity;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import cubyz.utils.json.JsonObject;
import cubyz.world.ServerWorld;
import cubyz.world.blocks.Block;
import cubyz.world.items.Inventory;
import cubyz.world.items.tools.Tool;

/**
 * Anything that's not a block or a particle.
 */

public class Entity {

	protected ServerWorld world;

	protected Vector3f position = new Vector3f();
	protected Vector3f rotation = new Vector3f();
	public float vx, vy, vz;
	public float targetVX, targetVZ; // The velocity the AI wants the entity to have.

	protected float scale = 1f;
	public final float stepHeight;
	
	private final EntityType type;
	
	private final EntityAI entityAI;
	
	public float health, hunger;
	public final float maxHealth, maxHunger;

	public int id;

	private static int currentID = 0;
	
	/**
	 * Used as hitbox.
	 */
	public float width = 0.25f, height = 1.8f;
	
	/**
	 * @param type
	 * @param ai
	 * @param world
	 * @param maxHealth
	 * @param maxHunger
	 * @param stepHeight height the entity can move upwards without jumping.
	 */
	public Entity(EntityType type, EntityAI ai, ServerWorld world, float maxHealth, float maxHunger, float stepHeight) {
		this.type = type;
		this.world = world;
		this.maxHealth = health = maxHealth;
		this.maxHunger = hunger = maxHunger;
		this.stepHeight = stepHeight;
		entityAI = ai;

		id = currentID++;
	}
	
	public float getScale() {
		return scale;
	}
	
	/**
	 * Checks collision against all blocks within the hitbox and updates positions.
	 * @return The height of the step taken. Needed for hunger calculations.
	 */
	protected float collisionDetection(float deltaTime) {
		// Simulate movement in all directions and prevent movement in a direction that would get the player into a block:
		int minX = (int)Math.floor(position.x - width);
		int maxX = (int)Math.floor(position.x + width);
		int minY = (int)Math.floor(position.y);
		int maxY = (int)Math.floor(position.y + height);
		int minZ = (int)Math.floor(position.z - width);
		int maxZ = (int)Math.floor(position.z + width);
		float deltaX = vx*deltaTime;
		float deltaY = vy*deltaTime;
		float deltaZ = vz*deltaTime;
		Vector4f change = new Vector4f(deltaX, 0, 0, 0);
		float step = 0.0f;
		if(deltaX < 0) {
			int minX2 = (int)Math.floor(position.x - width + deltaX);
			// First check for partial blocks:
			for(int y = minY; y <= maxY; y++) {
				for(int z = minZ; z <= maxZ; z++) {
					checkBlock(minX, y, z, change);
				}
			}
			if(minX2 != minX && deltaX == change.x) {
				outer:
				for(int y = minY; y <= maxY; y++) {
					for(int z = minZ; z <= maxZ; z++) {
						if(checkBlock(minX2, y, z, change)) {
							change.x = 0;
							position.x = minX2 + 1.001f + width;
							break outer;
						}
					}
				}
			}
		} else if(deltaX > 0) {
			int maxX2 = (int)Math.floor(position.x + width + deltaX);
			// First check for partial blocks:
			for(int y = minY; y <= maxY; y++) {
				for(int z = minZ; z <= maxZ; z++) {
					checkBlock(maxX, y, z, change);
				}
			}
			if(maxX2 != maxX && deltaX == change.x) {
				outer:
				for(int y = minY; y <= maxY; y++) {
					for(int z = minZ; z <= maxZ; z++) {
						if(checkBlock(maxX2, y, z, change)) {
							change.x = 0;
							position.x = maxX2 - 0.001f - width;
							break outer;
						}
					}
				}
			}
		}
		position.x += change.x;
		if(deltaX != change.x) {
			vx = 0;
			change.w = 0; // Don't step if the player walks into a wall.
		}
		step = Math.max(step, change.w);
		change.x = 0;
		change.y = deltaY;
		minX = (int)Math.floor(position.x - width);
		maxX = (int)Math.floor(position.x + width);
		if(deltaY < 0) {
			int minY2 = (int)Math.floor(position.y + deltaY);
			// First check for partial blocks:
			for(int x = minX; x <= maxX; x++) {
				for(int z = minZ; z <= maxZ; z++) {
					checkBlock(x, minY, z, change);
				}
			}
			if(minY2 != minY && deltaY == change.y) {
				outer:
				for(int x = minX; x <= maxX; x++) {
					for(int z = minZ; z <= maxZ; z++) {
						if(checkBlock(x, minY2, z, change)) {
							change.y = 0;
							position.y = minY2 + 1.001f;
							break outer;
						}
					}
				}
			}
		} else if(deltaY > 0) {
			int maxY2 = (int)Math.floor(position.y + height + deltaY);
			// First check for partial blocks:
			for(int x = minX; x <= maxX; x++) {
				for(int z = minZ; z <= maxZ; z++) {
					checkBlock(x, maxY, z, change);
				}
			}
			if(maxY2 != maxY && deltaY == change.y) {
				outer:
				for(int x = minX; x <= maxX; x++) {
					for(int z = minZ; z <= maxZ; z++) {
						if(checkBlock(x, maxY2, z, change)) {
							change.y = 0;
							position.y = maxY2 - 0.001f - height;
							break outer;
						}
					}
				}
			}
		}
		position.y += change.y;
		if(deltaY != change.y) {
			stopVY();
		}
		change.w = 0; // Don't step in y-direction.
		step = Math.max(step, change.w);
		change.y = 0;
		change.z = deltaZ;
		minY = (int)Math.floor(position.y);
		maxY = (int)Math.floor(position.y + height);
		if(deltaZ < 0) {
			int minZ2 = (int)Math.floor(position.z - width + deltaZ);
			// First check for partial blocks:
			for(int x = minX; x <= maxX; x++) {
				for(int y = minY; y <= maxY; y++) {
					checkBlock(x, y, minZ, change);
				}
			}
			if(minZ2 != minZ && change.z == deltaZ) {
				outer:
				for(int x = minX; x <= maxX; x++) {
					for(int y = minY; y <= maxY; y++) {
						if(checkBlock(x, y, minZ2, change)) {
							change.z = 0;
							position.z = minZ2 + 1.001f + width;
							break outer;
						}
					}
				}
			}
		} else if(deltaZ > 0) {
			int maxZ2 = (int)Math.floor(position.z + width + deltaZ);
			// First check for partial blocks:
			for(int x = minX; x <= maxX; x++) {
				for(int y = minY; y <= maxY; y++) {
					checkBlock(x, y, maxZ, change);
				}
			}
			if(maxZ2 != maxZ && deltaZ == change.z) {
				outer:
				for(int x = minX; x <= maxX; x++) {
					for(int y = minY; y <= maxY; y++) {
						if(checkBlock(x, y, maxZ2, change)) {
							change.z = 0;
							position.z = maxZ2 - 0.001f - width;
							break outer;
						}
					}
				}
			}
		}
		position.z += change.z;
		if(deltaZ != change.z) {
			vz = 0;
			change.w = 0; // Don't step if the player walks into a wall.
		}
		step = Math.max(step, change.w);
		// And finally consider the stepping component:
		position.y += step;
		if(step != 0) vy = 0;
		return step;
	}
	
	/**
	 * All damage taken should get channeled through this function to remove redundant checks if the entity is dead.
	 * @param amount
	 */
	public void takeDamage(float amount) {
		health -= amount;
		if(health <= 0) {
			type.die(this);
		}
	}
	
	public void stopVY() {
		takeDamage(calculateFallDamage());
		vy = 0;
	}
	
	public int calculateFallDamage() {
		if(vy < 0)
			return (int)(8*vy*vy/900);
		return 0;
	}
	
	public boolean checkBlock(int x, int y, int z, Vector4f displacement) {
		Block b = getBlock(x, y, z);
		if(b != null && b.isSolid()) {
			if(b.mode.changesHitbox()) {
				return b.mode.checkEntityAndDoCollision(this, displacement, x, y, z, getBlockData(x, y, z));
			}
			// Check for stepping:
			if(y + 1 - position.y > 0 && y + 1 - position.y <= stepHeight) {
				displacement.w = Math.max(displacement.w, y + 1 - position.y);
				return false;
			}
			return true;
		}
		return false;
	}

	protected Block getBlock(int x, int y, int z) {
		return world.getBlock(x, y, z);
	}
	protected byte getBlockData(int x, int y, int z) {
		return world.getBlockData(x, y, z);
	}
	
	public boolean checkBlock(int x, int y, int z) {
		Block b = getBlock(x, y, z);
		if(b != null && b.isSolid()) {
			if(b.mode.changesHitbox()) {
				return b.mode.checkEntity(position, width, height, x, y, z, getBlockData(x, y, z));
			}
			return true;
		}
		return false;
	}
	
	public boolean isOnGround() {
		// Determine if the entity is on the ground by virtually displacing it by 0.2 below its current position:
		Vector4f displacement = new Vector4f(0, -0.2f, 0, 0);
		checkBlock((int)Math.floor(position.x), (int)Math.floor(position.y), (int)Math.floor(position.z), displacement);
		if(checkBlock((int)Math.floor(position.x), (int)Math.floor(position.y + displacement.y), (int)Math.floor(position.z), displacement)) {
			return true;
		}
		return displacement.y != -0.2f || displacement.w != 0;
	}
	
	public void hit(Tool weapon, Vector3f direction) {
		if(weapon == null) {
			takeDamage(1);
			vx += direction.x*0.2;
			vy += direction.y*0.2;
			vz += direction.z*0.2;
		} else {
			takeDamage(weapon.getDamage());
			// TODO: Weapon specific knockback.
			vx += direction.x*0.2;
			vy += direction.y*0.2;
			vz += direction.z*0.2;
		}
	}
	
	public void update(float deltaTime) {
		float step = collisionDetection(deltaTime);
		if(entityAI != null)
			entityAI.update(this);
		updateVelocity(deltaTime);

		// clamp health between 0 and maxHealth
		if (health < 0)
			health = 0;
		if (health > maxHealth)
			health = maxHealth;
		
		if(maxHunger > 0) {
			hungerMechanics(step, deltaTime);
		}
	}
	
	float oldVY = 0;
	/**
	 * Simulates the hunger system. TODO: Make dependent on mass
	 * @param step How high the entity stepped in this update cycle.
	 */
	protected void hungerMechanics(float step, float deltaTime) {
		// Passive energy consumption:
		hunger -= 0.00013333*deltaTime; // Will deplete hunger after 22 minutes of standing still.
		// Energy consumption due to movement:
		hunger -= (vx*vx + vz*vz)/900/16*deltaTime;
		
		// Jumping:
		if(oldVY < vy) { // Only care about positive changes.
			// Determine the difference in "signed" kinetic energy.
			float deltaE = vy*vy*Math.signum(vy) - oldVY*oldVY*Math.signum(oldVY);
			hunger -= deltaE/900;
		}
		oldVY = vy;
		
		// Stepping: Consider potential energy of the step taken V = m·g·h
		hunger -= ServerWorld.GRAVITY*step/900;
		
		// Examples:
		// At 3 blocks/second(player base speed) the cost of movement is about twice as high as the passive consumption.
		// So when walking on a flat ground in one direction without sprinting the hunger bar will be empty after 22/3≈7 minutes.
		// When sprinting however the speed is twice as high, so the energy consumption is 4 times higher, meaning the hunger will be empty after only 2 minutes.
		// Jumping takes 0.05 hunger on jump and on land.

		// Heal if hunger is more than half full:
		if(hunger > maxHunger/2 && health < maxHealth) {
			// Maximum healing effect is 1% maxHealth per second:
			float healing = Math.min(maxHealth*deltaTime*0.01f, maxHealth-health);
			health += healing;
			hunger -= healing;
		}
		// Eat into health when the hunger bar is empty:
		if(hunger < 0) {
			health += hunger;
			hunger = 0;
		}
	}
	
	protected void updateVelocity(float deltaTime) {
		// TODO: Use the entities mass, force and ground structure to calculate a realistic velocity change.
		vx += (targetVX-vx)/5*deltaTime;
		vz += (targetVZ-vz)/5*deltaTime;
		vy -= ServerWorld.GRAVITY*deltaTime;
	}
	
	// NDT related
	
	private Vector3f loadVector3f(JsonObject json) {
		float x = json.getFloat("x", 0);
		float y = json.getFloat("y", 0);
		float z = json.getFloat("z", 0);
		return new Vector3f(x, y, z);
	}
	
	private JsonObject saveVector(Vector3f vec) {
		JsonObject json = new JsonObject();
		json.put("x", vec.x);
		json.put("y", vec.y);
		json.put("z", vec.z);
		return json;
	}
	
	public JsonObject save() {
		JsonObject json = new JsonObject();
		json.put("id", type.getRegistryID().toString());
		json.put("position", saveVector(position));
		json.put("rotation", saveVector(rotation));
		json.put("velocity", saveVector(new Vector3f(vx, vy, vz)));
		json.put("health", health);
		json.put("hunger", hunger);
		return json;
	}
	
	public void loadFrom(JsonObject json) {
		position = loadVector3f(json.getObjectOrNew("position"));
		rotation = loadVector3f(json.getObjectOrNew("rotation"));
		Vector3f velocity = loadVector3f(json.getObjectOrNew("velocity"));
		vx = velocity.x;
		vy = velocity.y;
		vz = velocity.z;
		health = json.getFloat("health", maxHealth);
		hunger = json.getFloat("hunger", maxHunger);
	}
	
	public EntityType getType() {
		return type;
	}
	
	public ServerWorld getWorld() {
		return world;
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public Vector3f getRenderPosition() { // default method for render pos
		return new Vector3f(position.x, position.y + height/2, position.z);
	}
	
	public void setPosition(Vector3i position) {
		this.position.x = position.x;
		this.position.y = position.y;
		this.position.z = position.z;
	}
	
	public void setPosition(Vector3f position) {
		this.position = position;
	}
	
	public Vector3f getRotation() {
		return rotation;
	}
	
	public void setRotation(Vector3f rotation) {
		this.rotation = rotation;
	}
	
	public Inventory getInventory() {
		return null;
	}
	
	public static boolean aabCollision(float x1, float y1, float z1, float w1, float h1, float d1, float x2, float y2, float z2, float w2, float h2, float d2) {
		return x1 + w1 >= x2
				&& x1 <= x2 + w2
				&& y1 + h1 >= y2
				&& y1 <= y2 + h2
				&& z1 + d1 >= z2
				&& z1 <= z2 + d2;
	}
	
	/**
	 * @param vel
	 * @param x0
	 * @param y0
	 * @param z0
	 * @param w width in x direction
	 * @param h height in y direction
	 * @param d depth in z direction
	 * @param blockData
	 * @return
	 */
	public void aabCollision(Vector4f vel, float x0, float y0, float z0, float w, float h, float d, byte blockData) {
		// check if the displacement is inside the box:
		if(aabCollision(position.x - width + vel.x, position.y + vel.y, position.z - width + vel.z, width*2, height, width*2, x0, y0, z0, w, h, d)) {
			// Check if the entity can step on it:
			if(y0 + h - position.y > 0 && y0 + h - position.y <= stepHeight) {
				vel.w = Math.max(vel.w, y0 + h - position.y);
				return;
			}
			// Only collide if the previous position was outside:
			if(!aabCollision(position.x - width, position.y, position.z - width, width*2, height, width*2, x0, y0, z0, w, h, d)) {
				// Check in which direction the current displacement goes and changes accordingly:
				if(vel.x < 0) {
					vel.x = x0 + w - (position.x - width) + 0.01f;
				} else if(vel.x > 0) {
					vel.x = x0 - (position.x + width) - 0.01f;
				}
				else if(vel.y < 0) {
					vel.y = y0 + h - (position.y) + 0.01f;
				}
				else if(vel.y > 0) {
					vel.y = y0 - (position.y + height) - 0.01f;
				}
				else if(vel.z < 0) {
					vel.z = z0 + d - (position.z - width) + 0.01f;
				} else if(vel.z > 0) {
					vel.z = z0 - (position.z + width) - 0.01f;
				}
			}
		}
			
	}
	
}