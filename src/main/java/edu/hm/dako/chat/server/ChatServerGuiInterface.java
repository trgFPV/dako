package edu.hm.dako.chat.server;

/**
 * Interface, das der ServerGUI bereitstellen muss
 * 
 * @author Paul Mandl
 */
public interface ChatServerGuiInterface {

	public void showStartData(ServerStartData data);

	public void incrNumberOfLoggedInClients();

	public void decrNumberOfLoggedInClients();

	public void incrNumberOfRequests();
}
