import jade.core.*;
import jade.core.behaviours.*;
import jade.tools.ToolAgent;

public class SniffingAgent extends ToolAgent{
	public void toolSetup() {
		//#DOTNET_EXCLUDE_BEGIN
		// Send 'subscribe' message to the AMS
//		AMSSubscribe.addSubBehaviour(new SenderBehaviour(this, getSubscribe()));
//
//		// Handle incoming 'inform' messages
//		AMSSubscribe.addSubBehaviour(new SnifferAMSListenerBehaviour());
	}
}
