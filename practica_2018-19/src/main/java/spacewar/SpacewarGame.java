package spacewar;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.socket.TextMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SpacewarGame {

	//// public final static SpacewarGame INSTANCE = new SpacewarGame();

	private final static int FPS = 30;
	private final static long TICK_DELAY = 1000 / FPS;
	public final static boolean DEBUG_MODE = true;
	public final static boolean VERBOSE_MODE = true;
	
	private WebsocketGameHandler referencia;
	private Sala salita;

	ObjectMapper mapper = new ObjectMapper();
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	// GLOBAL GAME ROOM
	private Map<String, Player> players = new ConcurrentHashMap<>();
	private Map<Integer, Projectile> projectiles = new ConcurrentHashMap<>();
	private AtomicInteger numPlayers = new AtomicInteger();

	public SpacewarGame(WebsocketGameHandler h, Sala s) {
		referencia = h;
		salita = s;
		// modos de juego
	}

	public synchronized void addPlayer(Player player) {
		players.put(player.getSession().getId(), player);
		player.resetVida();
		player.resetPropulsion();
		int count = numPlayers.getAndIncrement();
		if (count == 0) {
			// this.startGameLoop();
		}
	}

	public Collection<Player> getPlayers() {
		return players.values();
	}

	public synchronized void removePlayer(Player player) throws IOException {
		Puntos uwu = new Puntos(player.getPlayerId(), player.getSession().getId(), player.getNombreNave(), player.getColorNave(), player.getSalaActual(), player.puntuacion);
		// comprobacion blablabla pdf 618
		if(referencia.rank.isEmpty())
			referencia.rank.add(uwu);
		else {
			Iterator<Puntos> it = referencia.rank.iterator();
			while(it.hasNext()) {
				
			}
		}
		
		salita.removePlayer(player.getPlayerId());
		try {
			players.remove(player.getSession().getId());
		
			//int count = this.numPlayers.decrementAndGet();
			if (this.players.size()<2) {
				System.out.println("se acabó la partida, procedemos a eliminar la sala");
				this.stopGameLoop();
				referencia.eliminarSala(salita.getNombreSala());
			}
		}catch(NullPointerException e) {
			System.out.println("SE INTENTA ELIMINAR A ALGUIEN QUE YA HA SIDO ELIMINADO");
		}
	}

	public void addProjectile(int id, Projectile projectile) {
		projectiles.put(id, projectile);
	}

	public Collection<Projectile> getProjectiles() {
		return projectiles.values();
	}

	public void removeProjectile(Projectile projectile) {
		players.remove(projectile.getId(), projectile);
	}

	public void startGameLoop() {
		System.out.println("startGameLoop");

		scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> tick(), TICK_DELAY, TICK_DELAY, TimeUnit.MILLISECONDS);
	}

	public void stopGameLoop() {
		if (scheduler != null) {
			scheduler.shutdown();
		}
	}

	public synchronized void broadcast(String message) throws IOException {
		for (Player player : getPlayers()) {
			try {
				player.getSession().sendMessage(new TextMessage(message.toString()));
			} catch (Throwable ex) {
				/*
				 * System.err.println("Execption sending message to player " +
				 * player.getSession().getId()); ex.printStackTrace(System.err);
				 */
				this.removePlayer(player);
			}
		}
	}

	private void tick() {

		ObjectNode json = mapper.createObjectNode();
		ObjectNode msg = mapper.createObjectNode();
		ArrayNode arrayNodePlayers = mapper.createArrayNode();
		ArrayNode arrayNodeProjectiles = mapper.createArrayNode();

		long thisInstant = System.currentTimeMillis();
		Set<Integer> bullets2Remove = new HashSet<>();
		Set<String> f = new HashSet<>();
		boolean removeBullets = false;
		boolean exitPlayer = false;
		boolean haMuerto = false;

		try {
			msg.put("event", "MUERTEEE");
			// Update players
			for (Player player : getPlayers()) {
				if (player.salir) {
					f.add(player.getSession().getId());
					exitPlayer = true;
					player.salir = false;
				}

				player.calculateMovement();

				ObjectNode jsonPlayer = mapper.createObjectNode();
				jsonPlayer.put("id", player.getPlayerId());
				jsonPlayer.put("shipType", player.getShipType());
				jsonPlayer.put("posX", player.getPosX());
				jsonPlayer.put("posY", player.getPosY());
				jsonPlayer.put("facingAngle", player.getFacingAngle());
				jsonPlayer.put("vida", player.getVida());
				jsonPlayer.put("nombre", player.getNombreNave());
				jsonPlayer.put("color", player.getColorNave());
				jsonPlayer.put("push", player.getPropulsion());
				jsonPlayer.put("points", player.puntuacion);
				arrayNodePlayers.addPOJO(jsonPlayer);
			}

			// Update bullets and handle collision
			for (Projectile projectile : getProjectiles()) {
				projectile.applyVelocity2Position();

				// Handle collision
				for (Player player : getPlayers()) {///////////// CONCURRENCIAAAAAAAAAAAAAA!!!!!!!!!!!!!!
					if ((projectile.getOwner().getPlayerId() != player.getPlayerId()) && player.intersect(projectile)) {
						// System.out.println("Player " + player.getPlayerId() + " was hit!!!");
						
						if (player.muerto()) {
							projectile.getOwner().puntuacion+= 30;
							System.out.println("He matado al jugador" + projectile.getOwner().puntuacion);
							Puntos uwu = new Puntos(player.getPlayerId(), player.getSession().getId(), player.getNombreNave(), player.getColorNave(), player.getSalaActual(), player.puntuacion);
							// comprobacion blablabla
							referencia.rank.add(uwu);
							
							haMuerto = true;
							System.out.println("Muere : "+ player.getNombreNave());
							f.add(player.getSession().getId());
							msg.put("id", player.getPlayerId());
						}
						else {
							projectile.getOwner().puntuacion+= 10;
							System.out.println("He dado al jugador");
							
							
							
							
							
						}
						projectile.setHit(true);
						break;
					}
					
				}

				ObjectNode jsonProjectile = mapper.createObjectNode();
				jsonProjectile.put("id", projectile.getId());

				if (!projectile.isHit() && projectile.isAlive(thisInstant)) {
					jsonProjectile.put("posX", projectile.getPosX());
					jsonProjectile.put("posY", projectile.getPosY());
					jsonProjectile.put("facingAngle", projectile.getFacingAngle());
					jsonProjectile.put("isAlive", true);
				} else {
					removeBullets = true;
					bullets2Remove.add(projectile.getId());
					jsonProjectile.put("isAlive", false);
					if (projectile.isHit()) {
						jsonProjectile.put("isHit", true);
						jsonProjectile.put("posX", projectile.getPosX());
						jsonProjectile.put("posY", projectile.getPosY());
					}
				}
				arrayNodeProjectiles.addPOJO(jsonProjectile);
			}
			
			json.put("event", "GAME STATE UPDATE");
			json.putPOJO("players", arrayNodePlayers); //// enviarle a los demas qu eha muerto
			json.putPOJO("projectiles", arrayNodeProjectiles);
			
			this.broadcast(json.toString());
			
			if(haMuerto) {
				this.broadcast(msg.toString());
			}

			if (removeBullets || exitPlayer) {
				//System.out.println("ELIMINO JUGADOR");
				this.projectiles.keySet().removeAll(bullets2Remove);
				synchronized (players) {
					this.players.keySet().removeAll(f);
					if (this.players.size()<2) {
						System.out.println("se acabó la partida, procedemos a eliminar la sala");
						this.stopGameLoop();
						referencia.eliminarSala(salita.getNombreSala());
					}
				}
			}
			
			/*if(this.players.size()<1) {
				//referencia a webgamehandler y borrar sala.
				referencia.eliminarSala(salita.getNombreSala());
			}*/


		} catch (Throwable ex) {

		}
	}

	public void handleCollision() {
		///
	}
}
