package at.tugraz.iaik.las.p2.ttp.client.ui;

import at.tugraz.iaik.las.p2.common.TagCrypto;
import at.tugraz.iaik.las.p2.ttp.client.BaseUi;
import at.tugraz.iaik.las.p2.ttp.client.TtpDataService;
import at.tugraz.iaik.las.p2.ttp.client.Utils;
import at.tugraz.iaik.las.p2.ttp.client.cmd.AllTagsCmd;
import at.tugraz.iaik.las.p2.ttp.client.data.TagJdo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;

/**
 * UI to add a new tag to the database of the TTP.
 * Capable of doing basic validation of input.
 * 
 * https://developers.google.com/web-toolkit/doc/latest/DevGuideUiCellTable
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class AddTagUi extends BaseUi {
	private Button btAdd;

	private Label lbName;
	private Label lbNameHint;
	private TextBoxBase tbName;

	private Label lbDescription;
	private Label lbDescriptionHint;
	private TextBoxBase tbDescription;

	private Label lbLongitude;
	private Label lbLongitudeHint;
	private TextBoxBase tbLongitude;

	private Label lbLatitude;
	private Label lbLatitudeHint;
	private TextBoxBase tbLatitude;

	private Label lbUid;
	private Label lbUidHint;
	private TextBoxBase tbUid;

	private Label lbTagCertificate;
	private Label lbTagCertificateHint;
	private FileUpload fuTagCertificate;

	private FormPanel plFileUpload;

	private TagJdo tagToAdd = null;

	private final String errorStyle = "labelError";

	public AddTagUi() {
		super("Add a new Tag", "Specify the properties of the new Tag.");
	}

	private void addTagBegin() {
		if (!AddTagUi.this.extractAndVerifyInput()) {
			return;
		}

		AddTagUi.this.plFileUpload.submit();
	}

	private void addTagFinish(String result) {
		int startIndex = result.indexOf(TtpDataService.START_TAG)
				+ TtpDataService.START_TAG.length();
		int endIndex = result.indexOf(TtpDataService.END_TAG);
		// AddTagUi.this.btUploadTagCertificate.setText(result +
		// ":::>>>"
		// + result.substring(startIndex, endIndex));
		String uploadedCertificateDbKey = result
				.substring(startIndex, endIndex);
		// AddTagUi.this.btUploadTagCertificate.setText("Uploaded. Click to re-upload.");

		AddTagUi.this.ttpDataService.addTag(this.tagToAdd,
				uploadedCertificateDbKey, new AsyncCallback<Boolean>() {

					@Override
					public void onSuccess(Boolean result) {
						(new AllTagsCmd()).execute();
					}

					@Override
					public void onFailure(Throwable caught) {
						// maybe display error
					}
				});
	}

	protected Widget createAndFormatContentPanel() {
		this.btAdd = new Button("Add");
		this.btAdd.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				AddTagUi.this.addTagBegin();
			}
		});

		this.lbName = new Label("Name");
		this.lbNameHint = new Label(
				"An abritrary name with three or more characters.");
		this.tbName = new TextBox();

		this.lbDescription = new Label("Description");
		this.lbDescriptionHint = new Label(
				"An abritrary description.");
		this.tbDescription = new TextBox();

		this.lbLongitude = new Label("Longitude");
		this.lbLongitudeHint = new Label(
				"Any double value between -180 and +180.");
		this.tbLongitude = new TextBox();

		this.lbLatitude = new Label("Latitude");
		this.lbLatitudeHint = new Label(
				"Any double value between -90 and +90.");
		this.tbLatitude = new TextBox();

		this.lbUid = new Label("UID");
		this.lbUidHint = new Label(
				"A hex string in the form of XXXXXXXXXXXXXX, where X=1..F (7 Bytes = 14 hex digits).");
		this.tbUid = new TextBox();

		this.lbTagCertificate = new Label(
				"Public Key Certificate File");
		this.lbTagCertificateHint = new Label(
				"Choose a valid X.509 Certificate stored on you PC.");
		this.fuTagCertificate = new FileUpload();

		this.plFileUpload = new FormPanel();
		
		final Grid grid = new Grid(7, 3);

		// file upload
		this.fuTagCertificate.setEnabled(true);
		// for some reason i must set a name, caused me a headache to find
		// this...
		this.fuTagCertificate.setName("TagCertificate");
		this.plFileUpload.setAction("/certificateUpload");
		this.plFileUpload.setEncoding(FormPanel.ENCODING_MULTIPART);
		this.plFileUpload.setMethod(FormPanel.METHOD_POST);
		this.plFileUpload.addSubmitCompleteHandler(new SubmitCompleteHandler() {

			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				if (!event.getResults().toLowerCase().contains("error")) {
					AddTagUi.this.addTagFinish(event.getResults());
				} else {
					AddTagUi.this.lbTagCertificateHint
							.setStylePrimaryName(AddTagUi.this.errorStyle);
				}

			}

		});
		this.plFileUpload.add(this.fuTagCertificate);

		grid.setWidget(0, 0, this.lbName);
		grid.setWidget(0, 1, this.tbName);
		grid.setWidget(0, 2, this.lbNameHint);
		grid.setWidget(1, 0, this.lbDescription);
		grid.setWidget(1, 1, this.tbDescription);
		grid.setWidget(1, 2, this.lbDescriptionHint);
		grid.setWidget(2, 0, this.lbLatitude);
		grid.setWidget(2, 1, this.tbLatitude);
		grid.setWidget(2, 2, this.lbLatitudeHint);
		grid.setWidget(3, 0, this.lbLongitude);
		grid.setWidget(3, 1, this.tbLongitude);
		grid.setWidget(3, 2, this.lbLongitudeHint);
		grid.setWidget(4, 0, this.lbUid);
		grid.setWidget(4, 1, this.tbUid);
		grid.setWidget(4, 2, this.lbUidHint);
		grid.setWidget(5, 0, this.lbTagCertificate);
		grid.setWidget(5, 1, this.plFileUpload);
		grid.setWidget(5, 2, this.lbTagCertificateHint);
		grid.setWidget(6, 1, this.btAdd);

		return grid;
	}

	/**
	 * 
	 * @param tag
	 * @return true if no errors occurred, otherwise false.
	 */
	private boolean extractAndVerifyInput() {
		boolean hasErrors = false;
		this.tagToAdd = new TagJdo();

		// >>> creation date
		this.tagToAdd.CreationDate = new java.util.Date();

		// >>> tag name
		this.tagToAdd.Name = AddTagUi.this.tbName.getValue();
		if (this.tagToAdd.Name.length() <= 2) {
			this.lbNameHint.setStylePrimaryName(this.errorStyle);
			hasErrors = true;
		} else {
			this.lbNameHint.removeStyleName(this.errorStyle);
		}

		// >>> tag description
		this.tagToAdd.Description = AddTagUi.this.tbDescription.getValue();

		// >>> latitude
		try {
			this.lbLatitudeHint.removeStyleName(this.errorStyle);
			this.tagToAdd.LocationLatitude = Double.parseDouble(AddTagUi.this.tbLatitude
					.getValue());
			if (this.tagToAdd.LocationLatitude < -90 || this.tagToAdd.LocationLatitude > 90) {
				this.lbLatitudeHint.setStylePrimaryName(this.errorStyle);
				hasErrors = true;
			}

		} catch (Exception e) {
			this.lbLatitudeHint.setStylePrimaryName(this.errorStyle);
			hasErrors = true;
		}

		// >>> longitude
		try {
			this.lbLongitudeHint.removeStyleName(this.errorStyle);
			this.tagToAdd.LocationLongitude = Double
					.parseDouble(AddTagUi.this.tbLongitude.getValue());
			if (this.tagToAdd.LocationLongitude < -180 || this.tagToAdd.LocationLongitude > 180) {
				this.lbLongitudeHint.setStylePrimaryName(this.errorStyle);
				hasErrors = true;
			}

		} catch (Exception e) {
			this.lbLongitudeHint.setStylePrimaryName(this.errorStyle);
			hasErrors = true;
		}

		// >>> UID
		this.lbUidHint.removeStyleName(this.errorStyle);
		byte[] uid = Utils.hexStringToByteArray(this.tbUid.getValue());
		if (uid == null || uid.length != TagCrypto.CRYPTO_TAG_UID_LENGTH) {
			this.lbUidHint.setStylePrimaryName(this.errorStyle);
			hasErrors = true;
		} else {
			this.tagToAdd.Uid = this.tbUid.getValue();
		}

		// >>> cert file
		this.lbTagCertificateHint.removeStyleName(this.errorStyle);
		if (this.fuTagCertificate.getFilename().isEmpty()) {
			this.lbTagCertificateHint.setStylePrimaryName(this.errorStyle);
			hasErrors = true;
		}

		return !hasErrors;
	}
}
