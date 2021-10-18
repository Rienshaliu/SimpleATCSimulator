import java.io.IOException;
import java.util.*;

import java.io.FileWriter;
import com.opencsv.CSVWriter;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class STCA extends Agent{
	// Put agent initializations here
	
	public static boolean GREEN = true;
	public static boolean RED = false;
	int FDNum = 10;
	
	private Map<String, Boolean> notification = new HashMap();
	private ArrayList<String> clearanceList = new ArrayList<String>();
	
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello! STCA "+getAID().getLocalName()+" is ready.");
		
		for (int i=0; i<FDNum; i++) {
			notification.put("fd"+i, RED);
		}
		
		addBehaviour(new NotificationPerformer());
		addBehaviour(new ReceiveClearanceInfo());
	}
	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("STCA "+getAID().getLocalName()+" terminating.");
	}
	private class NotificationPerformer extends Behaviour {
//		Behaviour sendNotif = new SendAlertSignal(myAgent, 1000);
		private int step = 0;
		long startTime;
		
		@Override
		public void action() {
			// TODO Auto-generated method stub
			switch (step) {
			case 0:
//				System.out.println("Runway status: "+ ATC.runwayStatus);
				for (int i=0; i<FDNum; i++) {
					notification.put("fd"+i, RED);
				}
				if (ATC.runwayStatus==ATC.FREE && clearanceList.size()>0) {
					notification.put(clearanceList.get(0), GREEN);
//					System.out.println(notification);
					step = 1;
					startTime = System.currentTimeMillis();
				}
				break;
			case 1:
				if ((System.currentTimeMillis() - startTime) < 4000) {
					if ((System.currentTimeMillis() - startTime)%1000 == 0) {
//						System.out.println(notification);
//						System.out.println(System.currentTimeMillis());
//						System.out.println(startTime);
						String msgText = "";
						for (int i=0; i<FDNum; i++) {
							ACLMessage notiMsg = new ACLMessage(ACLMessage.INFORM);
							notiMsg.addReceiver(new AID("fd"+i, AID.ISLOCALNAME));
							notiMsg.setConversationId("notification");
							if (notification.get("fd"+i)==GREEN) {
								notiMsg.setContent("GREEN");
								msgText = msgText + "G";
							}
							else {
								notiMsg.setContent("RED");
								msgText = msgText + "R";
							}
							myAgent.send(notiMsg);
						}
						CSVWriter writer;
						try {
							writer = new CSVWriter(new FileWriter("log.csv", true));
							//Create record
							String logText = "";
							logText = logText + System.currentTimeMillis()+","+"STCA"+","+"Broadcast"+","+ msgText;
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
				}
				else {
//					System.out.println(clearanceList.size());
					clearanceList.clear();;
					step = 0;
				}
				break;
			}
		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
	private class ReceiveClearanceInfo extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchConversationId("clearance-info");
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				//String content = msg.getContent();
//				System.out.println("STCA:"+": "+msg.getContent()+" from "+msg.getSender().getLocalName()+"!!");
				for (String receiver: notification.keySet()) {
					if (ATC.runwayStatus == ATC.FREE && receiver.equals(msg.getContent())) {
						if (!clearanceList.contains(receiver)) 
							clearanceList.add(receiver);
						break;
					}
				}
			}
			else {
				block();
			}
		}
	}
}
