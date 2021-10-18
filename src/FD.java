import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import com.opencsv.CSVWriter;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class FD extends Agent{
	
	Random rand = new Random();
	
	private AID ATCAgent = new AID("atc", AID.ISLOCALNAME);
	private int takeOffTime = 5000;
	private int id = 0;
//	private int step = 0;
	
	// Put agent initializations here
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello! Flight Desk "+getAID().getLocalName()+" is ready.");
		id = Integer.parseInt(getAID().getLocalName().substring(2));
		addBehaviour(new RequestPerformer());
		
	}
	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Flight Desk "+getAID().getLocalName()+" terminating.");
	}
	
	private class RequestPerformer extends Behaviour {
		private MessageTemplate mt;// The template to receive replies
		private int step = 0;
		long startTime;
		boolean land = false;

		public void action() {
			switch (step) {
			case 0:
				ACLMessage req = new ACLMessage(ACLMessage.INFORM);
				req.addReceiver(ATCAgent);
				req.setConversationId("landing-request");
				req.setContent("request");
				myAgent.send(req);
				
				CSVWriter writer;
//				try {
//					writer = new CSVWriter(new FileWriter("log.csv", true));
//					//Create record
//					String logText = "";
//					logText = logText + System.currentTimeMillis()+","+getAID().getLocalName()+","+"SDP"+","+"landingReq";
//					String [] record = logText.split(",");
//					//Write the record to file
//					writer.writeNext(record);
//					//close the writer
//					writer.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
				mt = MessageTemplate.MatchConversationId("landing-request");
				
				step = 1;
				break;
			case 1:
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					//String content = msg.getContent();
//					System.out.println("Flight Desk "+getAID().getLocalName()+": Reply received!!");
//					System.out.println(reply.getSender().getLocalName()+": "+reply.getContent());
//					String sender = msg.getSender().getLocalName();
					if (reply.getContent().equals("clearance")) {
						step = 2;
						mt = MessageTemplate.MatchConversationId("notification");
					}
					else
						step = 0;
				}
				else {
					block();
				}
				break;
			case 2:
				ACLMessage notification = myAgent.receive(mt);
				if (notification != null) {
					//String content = msg.getContent();
//					System.out.println("Flight Desk "+getAID().getLocalName()+": "+notification.getContent()+" from "+notification.getSender().getLocalName());
//					String sender = msg.getSender().getLocalName();
					
//					if (notification.getSender().getLocalName().equals("stca")) {
						if (notification.getContent().equals("GREEN"))
							step = 3;
						else
							step = 0;
//					}
				}
				else {
					block();
				}
				break;
			case 3:
				ACLMessage report = new ACLMessage(ACLMessage.INFORM);
				report.addReceiver(ATCAgent);
				report.setConversationId("action-report");
				report.setContent("action");
				myAgent.send(report);
				startTime = System.currentTimeMillis();
				
				try {
					writer = new CSVWriter(new FileWriter("log.csv", true));
					//Create record
					String logText = "";
					logText = logText + System.currentTimeMillis()+","+getAID().getLocalName()+","+"SDP"+","+"inaction";
					String [] record = logText.split(",");
					//Write the record to file
					writer.writeNext(record);
					//close the writer
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				step = 4;
				break;
			case 4:
				if (!land) {
					land = true;
					System.out.println(getAID().getLocalName() +" is landing");
					ATC.runwayStatus = ATC.BUSY;
				}
				if ((System.currentTimeMillis() - startTime) > takeOffTime) {
					report = new ACLMessage(ACLMessage.INFORM);
					report.addReceiver(ATCAgent);
					report.setConversationId("action-report");
					report.setContent("released");
					myAgent.send(report);
					
					try {
						writer = new CSVWriter(new FileWriter("log.csv", true));
						//Create record
						String logText = "";
						logText = logText + System.currentTimeMillis()+","+getAID().getLocalName()+","+"SDP"+","+"released";
						String [] record = logText.split(",");
						//Write the record to file
						writer.writeNext(record);
						//close the writer
						writer.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					System.out.println(getAID().getLocalName() +" landing finished");
					ATC.runwayStatus = ATC.FREE;
					step = 5;
				}
				break;
			}
		}
		public boolean done() {
			return (step == 5);
		}
	}
}
