package spacewar;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.WebSocketSession;

public class Sala {
	private int numMax = 4;
	private String nombreSala;
	private boolean lleno = false;
	private boolean vacia = true;
	private SpacewarGame sg;
	private Player leader;
	
	private ConcurrentHashMap<Integer, Player> jugadores = new ConcurrentHashMap<>();
	
	public String getNombreSala() {
		return nombreSala;
	}
	
	public synchronized void addPlayer(int id, Player p) {
		if(vacia) {
			leader = p;
		}
		vacia = false;
		jugadores.putIfAbsent(id, p);
		if(jugadores.size()==numMax) {
			lleno = true;
		}
	}
	
	public synchronized void removePlayer(int id) {
		jugadores.remove(id);
		if(jugadores.size()==0) {
			vacia = true;
		}
	}
	
	public void empezar() {
		for(Player p : jugadores.values()) {
			
			System.out.println("AÑADE EL JUGADOR AL JUEGO");
			
			
			this.sg.addPlayer(p);
		}
		this.sg.startGameLoop();
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
}
