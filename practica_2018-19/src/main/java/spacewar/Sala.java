package spacewar;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Sala {
	private int numMax = 4;
	private String nombreSala;
	private boolean lleno = false;
	private boolean vacia = true;
	private boolean empezada = false;
	private int prop = 100;
	
	
	public boolean isEmpezada() {
		return empezada;
	}

	public SpacewarGame sg;
	private int leader;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	public ConcurrentHashMap<Integer, Player> jugadores = new ConcurrentHashMap<>();
	
	public String getNombreSala() {
		return nombreSala;
	}
	
	public synchronized void addPlayer(int id, Player p) throws IOException {
		if(vacia) {
			leader = id;
		}else {
			ObjectNode msg = mapper.createObjectNode();
			msg.put("event", "BOTON EMPEZAR");
			jugadores.get(leader).getSession().sendMessage(new TextMessage(msg.toString()));
		}
		vacia = false;
		p.setSalaActual(nombreSala);
		p.prpMax = prop;
		jugadores.putIfAbsent(id, p);
		if(jugadores.size()==numMax) {
			lleno = true;
		}
	}
	
	public synchronized void removePlayer(int id) throws IOException {
		try {
			jugadores.get(id).setSalaActual(null);
			jugadores.remove(id);
			if(leader == id && !empezada && jugadores.size() != 0) {
				leader = jugadores.keys().nextElement();
				ObjectNode msg = mapper.createObjectNode();
				msg.put("event", "BOTON EMPEZAR");
				jugadores.get(leader).getSession().sendMessage(new TextMessage(msg.toString()));
			}
			if(jugadores.size()==0) {
				vacia = true;
				//terminar();
			}
		}catch(NullPointerException e) {
			//
		}
	}
	
	public synchronized void listo() throws IOException {//no hace falta(?)
		ObjectNode msg = mapper.createObjectNode();
		
		for(Player p : jugadores.values()) {
			System.out.println("Avisamos al JS que prepare el estado game");
			msg.put("event", "LISTO");
			p.getSession().sendMessage(new TextMessage(msg.toString()));
		}
	}
	
	public synchronized void sms(String mj) throws IOException {//no hace falta(?)
		for(Player p : jugadores.values()) {
			System.out.println(mj);
			p.getSession().sendMessage(new TextMessage(mj));
		}
	}
	
	public synchronized void empezar() {//no hace falta(?)
		empezada = true;
		for(Player p : jugadores.values()) {
			System.out.println("AÑADE EL JUGADOR AL JUEGO");
			
			this.sg.addPlayer(p);
		}
		this.sg.startGameLoop();
	}
	
	public void addBalas(int id, Projectile projectile) {
		sg.addProjectile(id, projectile);
	}
	
	public void terminar() {
		sg.stopGameLoop();
	}

	public boolean isFull() {
		/*if(jugadores.size()==numMax) {
			return true;
		}else {
			return false;
		}*/
		return jugadores.size()==numMax;
	}
	
	public boolean isVacia() {
		return vacia;
	}
	
	public Sala(String nombre, WebsocketGameHandler h, String mode) {
		this.nombreSala = nombre;
		this.sg = new SpacewarGame(h,this);
		
		switch (mode){
		case "A":
			numMax = 2;
			prop = 50;
			System.out.println("Modo de juego A");
			break;
		case "B":
			numMax = 6;
			prop = 200;
			System.out.println("Modo de juego B");
			break;
		default:
			System.out.println("NO RECIBE BIEN EL MODO!!!");
			break;
		}
	}
	
	public void brct(String msi) throws IOException {
		sg.broadcast(msi);
	}
}
