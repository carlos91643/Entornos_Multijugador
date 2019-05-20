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
	private SpacewarGame sg;
	private Player leader;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	private ConcurrentHashMap<Integer, Player> jugadores = new ConcurrentHashMap<>();
	
	public String getNombreSala() {
		return nombreSala;
	}
	
	public synchronized void addPlayer(int id, Player p) {
		if(vacia) {
			leader = p;
		}
		vacia = false;
		p.setSalaActual(nombreSala);
		jugadores.putIfAbsent(id, p);
		if(jugadores.size()==numMax) {
			lleno = true;
		}
	}
	
	public synchronized void removePlayer(int id) {
		jugadores.get(id).setSalaActual(null);
		jugadores.remove(id);
		if(jugadores.size()==0) {
			vacia = true;
			terminar();
		}
		//si queda 1 en la sala, que se a el leader
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
		return lleno;
	}
	
	public boolean isVacia() {
		return vacia;
	}
	
	public Sala(String nombre) {
		this.nombreSala = nombre;
		this.sg = new SpacewarGame();
	}
	
	public void brct(String msi) {
		sg.broadcast(msi);
	}
}
