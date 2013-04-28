package at.tugraz.iaik.las.p2.ttp.client.ui;

import java.util.List;

import at.tugraz.iaik.las.p2.ttp.client.BaseUi;
import at.tugraz.iaik.las.p2.ttp.client.cmd.AllTagsCmd;
import at.tugraz.iaik.las.p2.ttp.client.data.TagJdo;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionModel;

/**
 * Shows all Tags known to the TTP. Allows to delete Tags.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class AllTagsUi extends BaseUi {

	private Button btDelete;
	private DataGrid<TagJdo> table;
	private ListDataProvider<TagJdo> dataProvider;

	public AllTagsUi() {
		super(
				"All Tags",
				"Any Tags known to the TTP for which a Trusted Location Time Ticket might be issued.");
		this.refreshData();
	}

	private void refreshData() {
		this.ttpDataService.getAllTags(new AsyncCallback<List<TagJdo>>() {

			@Override
			public void onSuccess(List<TagJdo> result) {
				AllTagsUi.this.dataProvider.setList(result);
			}

			@Override
			public void onFailure(Throwable caught) {
				// ignore
				AllTagsUi.this.dataProvider.getList().clear();
			}
		});
	}

	protected Widget createAndFormatContentPanel() {
		this.btDelete = new Button("Delete");
		this.dataProvider = new ListDataProvider<TagJdo>();

		FlowPanel panel = new FlowPanel();
		panel.add(this.btDelete);
		this.btDelete.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				for (TagJdo tag : AllTagsUi.this.dataProvider.getList()) {
					if (AllTagsUi.this.table.getSelectionModel()
							.isSelected(tag)) {
						AllTagsUi.this.ttpDataService.deleteTag(tag,
								new AsyncCallback<Void>() {

									@Override
									public void onFailure(Throwable caught) {
										// ignore
										(new AllTagsCmd()).execute();
									}

									@Override
									public void onSuccess(Void result) {
										(new AllTagsCmd()).execute();
									}
								});
					}
				}
			}
		});

		// table with key provider
		this.table = new DataGrid<TagJdo>(TagJdo.KEY_PROVIDER);
		this.table.setWidth("100%");

		this.table.setEmptyTableWidget(new Label("No Tags found."));

		// selection model
		final SelectionModel<TagJdo> selectionModel = new MultiSelectionModel<TagJdo>(
				TagJdo.KEY_PROVIDER);
		this.table.setSelectionModel(selectionModel,
				DefaultSelectionEventManager.<TagJdo> createCheckboxManager());

		Column<TagJdo, Boolean> checkColumn = new Column<TagJdo, Boolean>(
				new CheckboxCell(true, false)) {
			@Override
			public Boolean getValue(TagJdo object) {
				return selectionModel.isSelected(object);
			}
		};
		this.table.addColumn(checkColumn,
				SafeHtmlUtils.fromSafeConstant("<br/>"));
		this.table.setColumnWidth(checkColumn, 40, Unit.PX);

		TextColumn<TagJdo> nameColumn = new TextColumn<TagJdo>() {

			@Override
			public String getValue(TagJdo object) {
				return object.Name;
			}
		};

		TextColumn<TagJdo> descriptionColumn = new TextColumn<TagJdo>() {

			@Override
			public String getValue(TagJdo object) {
				return object.Description;
			}
		};

		TextColumn<TagJdo> creationDateColumn = new TextColumn<TagJdo>() {
			@Override
			public String getValue(TagJdo object) {
				if (object.CreationDate == null) {
					return "null";
				}
				return object.CreationDate.toString();
			}
		};

		TextColumn<TagJdo> uidColumn = new TextColumn<TagJdo>() {
			@Override
			public String getValue(TagJdo object) {
				return object.Uid;
			}
		};

		Column<TagJdo, String> locationColumn = new Column<TagJdo, String>(
				new ButtonCell()) {
			@Override
			public String getValue(TagJdo object) {
				return object.LocationLatitude + "," + object.LocationLongitude;
			}
		};

		Column<TagJdo, String> publicKeyFileColumn = new Column<TagJdo, String>(
				new ButtonCell()) {
			@Override
			public String getValue(TagJdo object) {
				if (object.PublicKeyCertificateFile != null) {
					return object.PublicKeyCertificateFile.SourceFilename
							+ " ("
							+ object.PublicKeyCertificateFile.Bytes.length
							+ " Bytes)";
				} else {
					return "(none)";
				}
			}
		};

		publicKeyFileColumn.setFieldUpdater(new FieldUpdater<TagJdo, String>() {
			@Override
			public void update(int index, TagJdo object, String value) {
				StringBuilder sb = new StringBuilder();
				sb.append("/certificateDownload");
				sb.append("?key=" + object.PublicKeyCertificateFile.Key);
				com.google.gwt.user.client.Window.open(sb.toString(), "_blank",
						"");
			}
		});

		locationColumn.setFieldUpdater(new FieldUpdater<TagJdo, String>() {
			@Override
			public void update(int index, TagJdo object, String value) {
				String url = "https://maps.google.com/?ll=";
				url += object.LocationLatitude;
				url += ",";
				url += object.LocationLongitude;
				com.google.gwt.user.client.Window.open(url, "_blank", "");
			}
		});

		this.table.addColumn(nameColumn, "Name");
		this.table.addColumn(descriptionColumn, "Description");
		this.table.addColumn(locationColumn, "Location (Google Maps Link)");
		this.table.addColumn(uidColumn, "UID");
		this.table.addColumn(publicKeyFileColumn, "Public Key File");
		this.table.addColumn(creationDateColumn, "Created");

		this.dataProvider.addDataDisplay(table);

		ResizeLayoutPanel panel2 = new ResizeLayoutPanel();
		panel2.setHeight("100%");
		panel2.add(this.table);
		panel.add(panel2);

		return panel;
	}
}
