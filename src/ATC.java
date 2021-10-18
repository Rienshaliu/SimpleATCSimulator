import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import com.opencsv.CSVWriter;

public class ATC extends Agent{
	// Put agent initializations here
	
	Random rand = new Random();
	
	public static boolean FREE = true;
	public static boolean BUSY = false;
	
	private AID STCAAgent = new AID("stca", AID.ISLOCALNAME);
	ArrayList<String> reqList = new ArrayList<String>();
	public static boolean runwayStatus = FREE;
	Logger LOGGER = Logger.getLogger("InfoLogging");
	
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello! Air Traffic Controller "+getAID().getLocalName()+" is ready.");

		addBehaviour(new ReceiveReq());
		addBehaviour(new ReceiveReport());
		
	}
	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Air Traffic Controller "+getAID().getLocalName()+" terminating.");
	}
	private class ReceiveReq extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchConversationId("landing-request");
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				//String content = msg.getContent();
//				System.out.println("Controller: Landing request received from "+msg.getSender().getLocalName()+"!!");
				String sender = msg.getSender().getLocalName();
				
				ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
				reply.addReceiver(new AID(sender, AID.ISLOCALNAME));
				reply.setConversationId("landing-request");
				
				ACLMessage clearanceInfo = new ACLMessage(ACLMessage.INFORM);
				clearanceInfo.addReceiver(STCAAgent);
				clearanceInfo.setConversationId("clearance-info");
				
				String msgText = "";
				
				if (runwayStatus == FREE) {
					if (reqList.isEmpty() || sender.equals(reqList.get(0))) {
						if (!reqList.isEmpty()) reqList.remove(0);
						reply.setContent("clearance");
						msgText = sender;
//						runwayStatus = BUSY;
						clearanceInfo.setContent(sender);
//						myAgent.send(clearanceInfo);
					}
					else {							
						reply.setContent("wait");
					}
				}
				else {
					reply.setContent("wait");
					if (!reqList.contains(sender))
						reqList.add(sender);
				}
				myAgent.send(reply);
				if (!msgText.equals("")) {
					myAgent.send(clearanceInfo);
				
					CSVWriter writer;
					try {
						writer = new CSVWriter(new FileWriter("log.csv", true));
						//Create record
						String logText = "";
						logText = logText + System.currentTimeMillis()+","+"SDP"+","+"STCA"+","+msgText;
						String [] record = logText.split(",");
						//Write the record to file
						writer.writeNext(record);
						//close the writer
						writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
//				System.out.println("Request List: " + reqList.toString());
			}
			else {
				block();
			}
		}
	}
	private class ReceiveReport extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchConversationId("action-report");
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				//String content = msg.getContent();
//				System.out.println("Controller: action report received from "+msg.getSender().getLocalName()+"!!");
				String sender = msg.getSender().getLocalName();
//				runwayStatus = BUSY;
			}
			else {
				block();
			}
		}
	}
}
