package at.tugraz.iaik.las.p2.ttp.client.ui;

import java.util.List;

import at.tugraz.iaik.las.p2.ttp.client.BaseUi;
import at.tugraz.iaik.las.p2.ttp.client.data.ProtocolLogJdo;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

/**
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class IssuedTLttsUi extends BaseUi {
	
	private DataGrid<ProtocolLogJdo> table;
	private ListDataProvider<ProtocolLogJdo> dataProvider;
	private TextColumn<ProtocolLogJdo> timestampColumn;

	public IssuedTLttsUi() {
		super("Issued T-LTTs",
				"Lists all Trusted Location Time Tickets (T-LTTs) issued by the server.");

		this.refreshData();
	}

	private void refreshData() {
		this.ttpDataService
				.getIssuedTLttProtocolLogs(new AsyncCallback<List<ProtocolLogJdo>>() {

					@Override
					public void onSuccess(List<ProtocolLogJdo> result) {
						IssuedTLttsUi.this.dataProvider.getList().clear();
						IssuedTLttsUi.this.dataProvider.getList().addAll(result);
						// force sorting
						// (http://stackoverflow.com/a/11892234/1730765)
						ColumnSortEvent.fire(IssuedTLttsUi.this.table,
								IssuedTLttsUi.this.table.getColumnSortList());
					}

					@Override
					public void onFailure(Throwable caught) {
						// ignore
						IssuedTLttsUi.this.dataProvider.getList().clear();
					}
				});
	}

	protected Widget createAndFormatContentPanel() {
		FlowPanel panel = new FlowPanel();

		// table with key provider
		this.table = new DataGrid<ProtocolLogJdo>(ProtocolLogJdo.KEY_PROVIDER);
		this.table.setWidth("100%");
		
		this.table.setEmptyTableWidget(new Label("No issued Location Time Certificates found."));

		this.timestampColumn = new TextColumn<ProtocolLogJdo>() {
			@Override
			public String getValue(ProtocolLogJdo pLog) {
				return pLog.Timestamp.toString();
			}
		};
		this.timestampColumn.setSortable(true);

		TextColumn<ProtocolLogJdo> uidColumn = new TextColumn<ProtocolLogJdo>() {
			@Override
			public String getValue(ProtocolLogJdo pLog) {
				return pLog.Uid;
			}
		};

		TextColumn<ProtocolLogJdo> nonceColumn = new TextColumn<ProtocolLogJdo>() {
			@Override
			public String getValue(ProtocolLogJdo pLog) {
				return pLog.Nonce;
			}
		};

		TextColumn<ProtocolLogJdo> lttColumn = new TextColumn<ProtocolLogJdo>() {
			@Override
			public String getValue(ProtocolLogJdo pLog) {
				return pLog.Ltt;
			}
		};

		Column<ProtocolLogJdo, String> tLttColumn = new Column<ProtocolLogJdo, String>(
				new ButtonCell()) {
			@Override
			public String getValue(ProtocolLogJdo object) {
				if (object.TLtt != null) {
					byte[] bytes = object.TLtt.getValue().getBytes();
					return bytes.length + " Bytes";
				} else {
					return "(none)";
				}
			}
		};

		tLttColumn.setFieldUpdater(new FieldUpdater<ProtocolLogJdo, String>() {
			@Override
			public void update(int index, ProtocolLogJdo object, String value) {
				// RequestBuilder builder = new
				// RequestBuilder(RequestBuilder.GET,
				// "/certificateDownload");
				StringBuilder sb = new StringBuilder();
				sb.append("/tLttDownload");
				// sb.append("?filename=" +
				// object.PublicKeyCertificate.SourceFilename);
				sb.append("?key=" + object.Key);
				com.google.gwt.user.client.Window.open(sb.toString(), "_blank",
						"");
			}
		});

		this.table.addColumn(timestampColumn, "Time");
		this.table.addColumn(uidColumn, "Tag UID");
		this.table.addColumn(nonceColumn, "Nonce");
		this.table.addColumn(lttColumn, "LTT");
		this.table.addColumn(tLttColumn, "T-LTT");

		this.dataProvider = new ListDataProvider<ProtocolLogJdo>();
		this.dataProvider.addDataDisplay(table);
		
		ResizeLayoutPanel panel2 = new ResizeLayoutPanel();
		panel2.setHeight("100%");
		panel2.add(this.table);
		panel.add(panel2);

		return panel;
	}
}
