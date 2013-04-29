package at.gv.egiz.android.tltt.constants;

public class TLTT {

	// request codes for startActivityForResult()
	public static final int GET_NEW_TICKET = 1; // used to start
												// CreateTicketActivity
	public static final int ADD_FILE_TO_TICKET = 2; // used to start
													// FileChooserActivity
	public static final int SHOW_TICKET = 3; // used to start ShowTicketActivity
	public static int CHANGE_LOCATION_SETTINGS = 4; // launch the Settings Menu
													// to enable the Location
													// providers
	public static final int CHANGE_SETTINGS = 5;// launch the Settings Menu
	public static final int SEND_TICKET_TO_PEER_DEVICE = 6;
	// to enable

	// datafield for the created ticket, in intent
	public static final String LOCATION_TIME_TICKET = "LocationTimeTicket";

	// datafields for adding files
	public static final String FILE_PATH = "FileName";

	// datafield for specifying the name of the menu resource for this activity
	public static final String ACTIVITY_MENU_ID = "MenuId";

	// datafield for returning the action the user wants to do
	public static final String COMMAND = "Command";

	// different types of "COMMAND"
	public static final int COMMAND_SIGN = 0;
	public static final int COMMAND_SHARE = 1;

	// path to folder where tltt attachments are stored
	public static final String PATH_ATTACHMENTS = "/sdcard/locationTimeTickets/attachments/";

}
