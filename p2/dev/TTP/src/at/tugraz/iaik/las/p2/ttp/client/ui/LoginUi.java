package at.tugraz.iaik.las.p2.ttp.client.ui;

import at.tugraz.iaik.las.p2.ttp.client.BaseUi;
import at.tugraz.iaik.las.p2.ttp.client.FailedLoginDialogBox;
import at.tugraz.iaik.las.p2.ttp.client.cmd.StartCmd;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class LoginUi extends BaseUi {
	private Label lbUsername;

	private Label lbPassword;

	private TextBoxBase tbUsername;

	private TextBoxBase tbPassword;

	private Button btLogin;

	public LoginUi() {
		super("Login", "Sign in for viewing LTTs and managing tags.");

		this.btLogin.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String username = LoginUi.this.tbUsername.getValue();
				String password = LoginUi.this.tbPassword.getValue();
				LoginUi.this.ttpDataService.login(username, password,
						new AsyncCallback<Boolean>() {

							@Override
							public void onSuccess(Boolean result) {
								if (result == true) {
									new StartCmd().execute();
								} else {
									DialogBox dialog = new FailedLoginDialogBox();
									dialog.show();
								}
							}

							@Override
							public void onFailure(Throwable caught) {
							}
						});
			}
		});
	}
	
	public HTML html1 = new HTML("XXX");
	public HTML html2 = new HTML("XXX");
	public HTML html3 = new HTML("XXX");
	public HTML html4 = new HTML("XXX");
	public HTML html5 = new HTML("XXX");

	@Override
	protected Widget createAndFormatContentPanel() {
		this.lbUsername = new Label("User Name");
		this.lbPassword = new Label("Password");
		this.tbUsername = new TextBox();
		this.tbPassword = new PasswordTextBox();
		this.btLogin = new Button("Login");
		
		Grid grid = new Grid(3, 2);		
		grid.setWidget(0, 0, this.lbUsername);
		grid.setWidget(0, 1, this.tbUsername);
		grid.setWidget(1, 0, this.lbPassword);
		grid.setWidget(1, 1, this.tbPassword);
		grid.setWidget(2, 1, this.btLogin);
		
		return grid;
	}
}
