package spacewar;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketGameHandler extends TextWebSocketHandler {

	//////// private SpacewarGame game = SpacewarGame.INSTANCE;
	private static final String PLAYER_ATTRIBUTE = "PLAYER";
	private ObjectMapper mapper = new ObjectMapper();
	private AtomicInteger playerId = new AtomicInteger(0);
	private AtomicInteger projectileId = new AtomicInteger(0);
	/////
	private ConcurrentHashMap<WebSocketSession, Player> sessions = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, Sala> salas = new ConcurrentHashMap<>();
	public List<Puntos> rank = new ArrayList<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		Player player = new Player(playerId.incrementAndGet(), session);
		sessions.putIfAbsent(session, player);
		session.getAttributes().put(PLAYER_ATTRIBUTE, player);

		// ObjectNode msg = mapper.createObjectNode();
		// msg.put("event", "JOIN");
		// msg.put("id", player.getPlayerId());
		// msg.put("shipType", player.getShipType());
		// player.getSession().sendMessage(new TextMessage(msg.toString()));

		//////// game.addPlayer(player);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {// recibe los sms
																										// del Js
																										// (clientes)
		try {
			JsonNode node = mapper.readTree(message.getPayload());
			ObjectNode msg = mapper.createObjectNode();
			Player player = (Player) session.getAttributes().get(PLAYER_ATTRIBUTE);

			// System.out.println(node.get("event").asText());
			switch (node.get("event").asText()) {
			case "JOIN":
				// System.out.println(node.get("params").asText());
				player.setNombreNave(node.get("params").asText());
				player.setColorNave(node.get("color").asText());
				msg.put("event", "JOIN");
				msg.put("id", player.getPlayerId());
				msg.put("shipType", player.getShipType());
				// msg.put("nombreNave", player.getNombreNave());
				player.getSession().sendMessage(new TextMessage(msg.toString()));
				System.out.println(player.getNombreNave());
				break;
			case "CREAR":
				String nRoom = node.get("params").asText();
				String duplicado = "no";
				if (salaExiste(node.get("params").asText())) { // si la sala "Salita" existe, creamos otra llamada
																// "Salita.1"
					do {
						nRoom += ".1";
					} while (salaExiste(nRoom)); // si existe "Salita.1", creamos "Salita.1.1" etc...
					duplicado = "yes";
				}

				Sala salita = new Sala(nRoom, this, node.get("modo").asText()); //"Sala1", referencia al handler y "A"
				salita.addPlayer(player.getPlayerId(), player);
				salas.putIfAbsent(nRoom, salita);

				msg.put("event", "NEW ROOM");
				msg.put("room", nRoom);
				msg.put("duplicado", duplicado);
				player.getSession().sendMessage(new TextMessage(msg.toString()));

				System.out.println(nRoom);

				break;
			case "UNIRSE":
				synchronized (salas) {
					if (salaExiste(node.get("params").asText())) { // si la sala existe nos unimos a ella si se puede
						if(!salas.get(node.get("params").asText()).isEmpezada()) {// si la partida todavía no ha empezado
							
							if (salas.get(node.get("params").asText()).isFull()) { // sala llena
								msg.put("event", "SALA LLENA");

								// la primera comprobación es de si la partida ha empezado
								// que hacer cuando la sala está llena y esas cosas
								System.out.println("Sala llena: " + node.get("params").asText());

								player.getSession().sendMessage(new TextMessage(msg.toString()));
							} else {
								System.out.println("nos unimos a la sala " + node.get("params").asText());
								salas.get(node.get("params").asText()).addPlayer(player.getPlayerId(), player);
								
								msg.put("event", "CHAT");
								msg.put("nombre", player.getNombreNave());
								msg.put("mensaje", "Hey me he unido a esta sala");
								msg.put("colorsito", player.getColorNave());

								salas.get(node.get("params").asText()).sms(msg.toString());
								
								if (salas.get(node.get("params").asText()).isFull()) {
									System.out.println("EMPEZAMOS PERTIDA");
									synchronized (salas.get(node.get("params").asText())) {
										salas.get(node.get("params").asText()).listo();
										// ------------------------------ aquí algún jugador podría salir de la sala
										salas.get(node.get("params").asText()).empezar();
									}
								}
							}
							
						}else { // la partida ya ha empezado, así que busca otra
							System.out.println("la sala no existe " + node.get("params").asText());

							msg.put("event", "UNIRSE");
							player.getSession().sendMessage(new TextMessage(msg.toString()));
						}
						
					} else { // si no existe volvemos al JS para preguntar otro nombre
						System.out.println("la sala no existe " + node.get("params").asText());

						msg.put("event", "UNIRSE");
						player.getSession().sendMessage(new TextMessage(msg.toString()));
					}
				}
				break;
			case "SALIR":
				try {
					synchronized (salas.get(node.get("params").asText())) {
	
						System.out.println("######################### ME QUIERO IR");
						System.out.println("NOMBRE SALA: " + node.get("params").asText());
						
						player.salir = true;
						player.vida = 0;
					}
				}catch(NullPointerException e) {
					//la sala ya se eliminó así que gg
				}
					/*salas.get(node.get("params")).removePlayer(player.getPlayerId());

					msg.put("event", "REMOVE PLAYER");
					msg.put("id", node.get("id").asInt());
					salas.get(node.get("params")).brct(msg.toString());

					if (salas.get(node.get("params")).isVacia()) {
						salas.remove(node.get("params"));
					}*/
				break;
			case "UPDATE MOVEMENT":
				player.loadMovement(node.path("movement").get("thrust").asBoolean(),
						node.path("movement").get("brake").asBoolean(),
						node.path("movement").get("rotLeft").asBoolean(),
						node.path("movement").get("rotRight").asBoolean(), node.path("push").asBoolean());
				if (node.path("bullet").asBoolean()) {
					Projectile projectile = new Projectile(player, this.projectileId.incrementAndGet());
					try {
						salas.get(node.get("nomSal").asText()).addBalas(projectile.getId(), projectile);
					}catch(NullPointerException e) {
						System.out.println(salas);
						System.out.println(node.get("nomSal").asText());
					}
				}

				break;
			case "EMPEZAR":
				System.out.println("EMPEZAMOS PERTIDA");
				synchronized (salas.get(node.get("params").asText())) {
					salas.get(node.get("params").asText()).listo();
					// ------------------------------ aquí algún jugador podría salir de la sala
					salas.get(node.get("params").asText()).empezar();
				}
				break;
			case "CHAT":
				/*
				 * for(Player p : sessions.values()) { //chat público
				 * p.getSession().sendMessage(new TextMessage(msg.toString())); }
				 */
				System.out.println(node.get("params").asText());

				msg.put("event", "CHAT");
				msg.put("nombre", node.get("name").asText());
				msg.put("mensaje", node.get("params").asText());
				msg.put("colorsito", node.get("color").asText());

				salas.get(node.get("sala").asText()).sms(msg.toString()); // chat exclusivo de la sala
				break;
			default:
				break;
			}

		} catch (Exception e) {
			System.err.println("Exception processing message " + message.getPayload());
			e.printStackTrace(System.err);
		}
	}

	public synchronized boolean salaExiste(String ro) {// no hace falta(?)
		if (salas.containsKey(ro)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void eliminarSala(String n) {
		try {
			salas.remove(n);
			System.out.println("La sala se elimina con exito");
		}catch(NullPointerException e) {
			System.out.println("La sala ya ha sido eliminada"); 
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		Player player = (Player) session.getAttributes().get(PLAYER_ATTRIBUTE);
		String sala = player.getSalaActual();
		sessions.remove(session);
		//////////////// game.removePlayer(player);
		if (sala != null) {
			synchronized (salas.get(sala)) {
				salas.get(sala).removePlayer(player.getPlayerId());
				if(salas.get(sala).isEmpezada()) {
					salas.get(sala).sg.removePlayer(player);
					
					ObjectNode msg = mapper.createObjectNode();
					msg.put("event", "REMOVE PLAYER");
					msg.put("id", player.getPlayerId());
					/////////////// game.broadcast(msg.toString());
					salas.get(sala).brct(msg.toString());
				}

				if (salas.get(sala).isVacia()) {
					try {
						salas.remove(sala);
						System.out.println("La sala se elimina");
					}catch(NullPointerException e){
						System.out.println("La sala ya ha sido eliminada"); 
					}
				}
			}
		}
	}
}
