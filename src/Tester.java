import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

public class Tester extends Agent{
	
	private AID STCAAgent = new AID("stca", AID.ISLOCALNAME);
	ArrayList<String> aircrafts = new ArrayList<String>();
	int fdNum = 10;
	
	int lineID = 1;
	boolean systemResponseCheck = false;
	int releaseLine = 0;
	int inactionLine = 0;
	
	// property could be receive 2 action reports within takeoffTime
	
	protected void setup() {
		for (int i=0; i<fdNum; i++) {
			aircrafts.add("fd"+i);
		}
//		addBehaviour(new Eavesdrop());
		addBehaviour(new TickerBehaviour(this, 500) {
			protected void onTick() {
//				ACLMessage notiMsg = new ACLMessage(ACLMessage.INFORM);
//				for (int i=0; i<aircrafts.length; i++) {
//					notiMsg.addReceiver(new AID(aircrafts[i], AID.ISLOCALNAME));
//					notiMsg.setConversationId("notification");
//					if (i < 2)
//						notiMsg.setContent("GREEN");
//					else
//						notiMsg.setContent("RED");
//					myAgent.send(notiMsg);
//				}
				try {
					CSVReader reader = new CSVReader(new FileReader("log.csv"));
				    //Read CSV line by line and use the string array as you want
					List<String[]> lines = reader.readAll();
//					System.out.println(lines.get(1)[2]);
					for (int i=lineID; i<lines.size(); i++) {
						if (lines.get(i)[3].equals("RRGRRRRRRR")) {
							System.out.println("Precondition is acheived!");
							for (int j=0; j<aircrafts.size(); j++) {
								ACLMessage notiMsg = new ACLMessage(ACLMessage.INFORM);
								notiMsg.addReceiver(new AID(aircrafts.get(j), AID.ISLOCALNAME));
								notiMsg.setConversationId("notification");
								if (j==0 || j==2)
									notiMsg.setContent("GREEN");
								else
									notiMsg.setContent("RED");
								myAgent.send(notiMsg);
							}
							systemResponseCheck = true;
						}
						if (systemResponseCheck) {
							if (lines.get(i)[1].equals("fd0") && lines.get(i)[2].equals("SDP") && lines.get(i)[3].equals("inaction"))
								inactionLine = i;
							if (lines.get(i)[1].equals("fd2") && lines.get(i)[2].equals("SDP") && lines.get(i)[3].equals("released"))
								releaseLine = i;
						}
					}
					lineID = lines.size();
					
					if (systemResponseCheck && inactionLine < releaseLine && inactionLine != 0) {
						System.out.println("Expected system response is acheived!");
					}
					
//					System.out.println(lineID);
//					while ((nextLine = reader.readNext()) != null) {
//						if (nextLine != null) {
//							//Verifying the read data here
//							System.out.println(Arrays.toString(nextLine));
//						}
//					}
					reader.close();
				} catch (CsvException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} );
	}
	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Tester-agent "+getAID().getName()+" terminating.");
	}
	private class Eavesdrop extends CyclicBehaviour {

		@Override
		public void action() {
			// TODO Auto-generated method stub
			MessageTemplate mt = MessageTemplate.MatchSender(STCAAgent);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				//String content = msg.getContent();
				System.out.println(msg.getSender().getLocalName()+": "+msg.getContent());
			}
			else {
				block();
			}
		}
		
	}
}
