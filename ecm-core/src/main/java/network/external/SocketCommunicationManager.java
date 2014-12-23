package network.external;

import java.net.InetSocketAddress;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import ecm.Ecm;

// TODO: Auto-generated Javadoc
/**
 * The Class CommunicationManager.
 */
public class SocketCommunicationManager implements CommunicationManager {

	/** The external plugin sw component. */
	private Ecm ecm;

	/** The session. */
	private IoSession session;

	/** The vin. */
	private String vin;

	private String server;

	private int port;

	/**
	 * Instantiates a new communication manager.
	 * 
	 * @param externalPluginSWComponent
	 *            the external plugin sw component
	 * @param vin
	 *            the vin
	 */
	public SocketCommunicationManager(String vin, String server, int port) {
		this.vin = vin;
		this.server = server;
		this.port = port;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		boolean connectServer = false;
		if (!server.equals("none")) {
			NioSocketConnector connector = new NioSocketConnector();
			connector.getSessionConfig().setKeepAlive(true);

			connector.getFilterChain().addLast(
					"codec",
					new ProtocolCodecFilter(
							new ObjectSerializationCodecFactory()));
			connector.getFilterChain().addLast("logger", new LoggingFilter());
			connector.setHandler(new ClientHandler(this));

			connectServer = true;

			boolean isStart = false;

			while (true) {
				try {
					ConnectFuture future = connector
							.connect(new InetSocketAddress(server, port));
					future.awaitUninterruptibly();
					session = future.getSession();
					isStart = true;
				} catch (Exception e) {
					System.out
							.println("Warning! Client failed to connect Server");
					System.out.println(e.toString());
					// System.exit(-1);
					isStart = false;
				}
				if (isStart) {
					System.out.println("Connected to trusted server");
					break;
				}
					
				else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
			if (connectServer) {
				session.getCloseFuture().awaitUninterruptibly();
				connector.dispose();
			}
		}
	}

	/**
	 * Gets the session.
	 * 
	 * @return the session
	 */
	public IoSession getSession() {
		return session;
	}

	/**
	 * Sets the session.
	 * 
	 * @param session
	 *            the new session
	 */
	public void setSession(IoSession session) {
		this.session = session;
	}

	/**
	 * Gets the vin.
	 * 
	 * @return the vin
	 */
	public String getVin() {
		return vin;
	}

	public void write(Object data) {
		session.write(data);
	}

	public Ecm getEcm() {
		return ecm;
	}

	@Override
	public void setEcm(Ecm ecm) {
		this.ecm = ecm;
	}

}