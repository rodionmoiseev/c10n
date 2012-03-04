package c10n;



/**
 * <p>Utility class for generating implementations
 * of c10n message interfaces annotated with &#64;{@link C10NMessages}.</p>
 * 
 * <p>Usage similar to logger creating in frameworks 
 * like log4j or slf4j.</p>
 * 
 * <p>Sample usage:
 * <pre>
 *   -- MyMessages.java
 *     import c10n.C10NMessages;
 *     
 *     &#64;C10NMessages
 *     public interface MyMessages {
 *     
 *       &#64;C10NDef("Hello, {0}")
 *       String title(String who);
 *       
 *       &#64;C10NDef("OK")
 *       String ok();
 *       
 *       &#64;C10NDef("Cancel")
 *       String cancel();
 *     }
 *     
 *   -- MyApplication.java
 *     import c10n.C10N;
 *     import javax.swing.*;
 *   
 *     class MyApplication {
 *       private static final MyMessages msg = C10N.get(MyMessages.class);
 *       
 *       public static void main(String[] args){
 *         JFrame frame = new JFrame(msg.title("World"));
 *         JButton okButton = new JButton(msg.ok());
 *         JButton cancelButton = new JButton(msg.cancel());
 *         ...
 *       }
 *     }</pre>
 *     
 * Implementation of <code>MyMessages</code> class is generated and will
 * return messages from configured bundle (TODO configuration).
 * </p>
 * 
 * @author rodion
 */
public final class C10N{
	private static C10NFactory root = new DefaultC10NFactory();

	public static C10NFactory getRootFactory(){
		return root;
	}
	
	public static void setRootFactory(C10NFactory newRoot){
		root = newRoot;
	}
	
	public static <T> T get(Class<T> c10nInterface){
		return root.get(c10nInterface);
	}

	public static void configure(AbstractC10NConfiguration conf){
		root.configure(conf);
	}
}
