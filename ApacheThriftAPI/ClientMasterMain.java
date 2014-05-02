import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import rso.at.ClientMasterService;


public class ClientMasterMain {

	private void start() {
		try {
			TServerSocket serverTransport = new TServerSocket(7911);

			ClientMasterService.Processor processor = 
					new ClientMasterService.Processor(new ClientMasterImpl());

			TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).
					processor(processor));
			System.out.println("Starting server on port 7911 ...");
			server.serve();
		} catch (TTransportException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ClientMasterMain srv = new ClientMasterMain();
		srv.start();
	}

}
