package at.tugraz.iaik.las.p2.ttp.client.ui;

import java.util.List;

import at.tugraz.iaik.las.p2.ttp.client.BaseUi;
import at.tugraz.iaik.las.p2.ttp.client.cmd.CompleteLogCmd;
import at.tugraz.iaik.las.p2.ttp.client.data.ProtocolLogJdo;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

/**
 * Shows the log of requests to the TTP and allows
 * to retrace protocol steps.
 * 
 * @author christian.lesjak@student.tugraz.at
 * 
 */
public class CompleteLogUi extends BaseUi {

	private DataGrid<ProtocolLogJdo> table;
	private ListDataProvider<ProtocolLogJdo> dataProvider;
	private Button btDelete;

	public CompleteLogUi() {
		super("Full Log", "Lists any requests to the server.");

		this.refreshData();
	}

	private void refreshData() {
		this.ttpDataService
				.getAllProtocolLogs(new AsyncCallback<List<ProtocolLogJdo>>() {

					@Override
					public void onSuccess(List<ProtocolLogJdo> result) {
						CompleteLogUi.this.dataProvider.getList().clear();
						CompleteLogUi.this.dataProvider.getList()
								.addAll(result);
						// force sorting
						// (http://stackoverflow.com/a/11892234/1730765)
						ColumnSortEvent.fire(CompleteLogUi.this.table,
								CompleteLogUi.this.table.getColumnSortList());
					}

					@Override
					public void onFailure(Throwable caught) {
						// ignore
						CompleteLogUi.this.dataProvider.getList().clear();
					}
				});
	}

	protected Widget createAndFormatContentPanel() {
		this.dataProvider = new ListDataProvider<ProtocolLogJdo>();	
		this.btDelete = new Button("Clear Log");
		
		FlowPanel panel = new FlowPanel();

		panel.add(this.btDelete);
		this.btDelete.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				CompleteLogUi.this.ttpDataService
						.clearLog(new AsyncCallback<Void>() {

							@Override
							public void onFailure(Throwable caught) {
								(new CompleteLogCmd()).execute();
							}

							@Override
							public void onSuccess(Void result) {
								(new CompleteLogCmd()).execute();
							}
						});
			}
		});

		// table with key provider
		this.table = new DataGrid<ProtocolLogJdo>(ProtocolLogJdo.KEY_PROVIDER);
		this.table.setWidth("100%");
		
		this.table.setEmptyTableWidget(new Label("No logs found."));

		Column<ProtocolLogJdo, String> timestampColumn = new Column<ProtocolLogJdo, String>(
				new TextCell()) {
			@Override
			public String getValue(ProtocolLogJdo pLog) {
				return pLog.Timestamp.toString();
			}
		};

		Column<ProtocolLogJdo, String> uidColumn = new Column<ProtocolLogJdo, String>(
				new TextCell()) {
			@Override
			public String getValue(ProtocolLogJdo pLog) {
				return pLog.Uid;
			}
		};

		Column<ProtocolLogJdo, String> nonceColumn = new Column<ProtocolLogJdo, String>(
				new TextCell()) {
			@Override
			public String getValue(ProtocolLogJdo pLog) {
				return pLog.Nonce;
			}
		};

		Column<ProtocolLogJdo, String> stepColumn = new Column<ProtocolLogJdo, String>(
				new TextCell()) {
			@Override
			public String getValue(ProtocolLogJdo pLog) {
				return pLog.ProtocolStep.toString();
			}
		};

		Column<ProtocolLogJdo, String> statusColumn = new Column<ProtocolLogJdo, String>(
				new TextCell()) {
			@Override
			public String getValue(ProtocolLogJdo pLog) {
				return pLog.StatusMessage;
			}
		};

		this.table.addColumn(timestampColumn, "Time");
		this.table.setColumnWidth(timestampColumn, 16, Unit.EM);
		this.table.addColumn(uidColumn, "Tag UID");
		this.table.setColumnWidth(uidColumn, 12, Unit.EM);
		this.table.addColumn(nonceColumn, "Nonce");
		this.table.setColumnWidth(nonceColumn, 22, Unit.EM);
		this.table.addColumn(stepColumn, "Protocol Step");
		this.table.setColumnWidth(stepColumn, 10, Unit.EM);
		this.table.addColumn(statusColumn, "Status Message");

		this.dataProvider.addDataDisplay(this.table);

		ResizeLayoutPanel panel2 = new ResizeLayoutPanel();
		panel2.setHeight("100%");
		panel2.add(this.table);
		panel.add(panel2);

		return panel;

		// this.timestampColumn = new TextColumn<ProtocolLogJdo>() {
		// @Override
		// public String getValue(ProtocolLogJdo pLog) {
		// return pLog.Timestamp.toString();
		// }
		// };
		// this.timestampColumn.setSortable(true);
		//
		// TextColumn<ProtocolLogJdo> uidColumn = new
		// TextColumn<ProtocolLogJdo>() {
		// @Override
		// public String getValue(ProtocolLogJdo pLog) {
		// return pLog.Uid;
		// }
		// };
		//
		// TextColumn<ProtocolLogJdo> nonceColumn = new
		// TextColumn<ProtocolLogJdo>() {
		// @Override
		// public String getValue(ProtocolLogJdo pLog) {
		// return pLog.Nonce;
		// }
		// };
		//
		// TextColumn<ProtocolLogJdo> stepColumn = new
		// TextColumn<ProtocolLogJdo>() {
		// @Override
		// public String getValue(ProtocolLogJdo pLog) {
		// return pLog.ProtocolStep.toString();
		// }
		// };
		//
		// TextColumn<ProtocolLogJdo> statusColumn = new
		// TextColumn<ProtocolLogJdo>() {
		// @Override
		// public String getValue(ProtocolLogJdo pLog) {
		// return pLog.StatusMessage;
		// }
		// };
		//
		// this.table.addColumn(timestampColumn, "Time");
		// this.table.addColumn(uidColumn, "Tag UID");
		// this.table.addColumn(nonceColumn, "Nonce");
		// this.table.addColumn(stepColumn, "Protocol Step");
		// this.table.addColumn(statusColumn, "Status Message");
		//
		// this.dataProvider.addDataDisplay(table);
		//
		// // Add a ColumnSortEvent.ListHandler to connect sorting to the
		// // java.util.List.
		// ListHandler<ProtocolLogJdo> columnSortHandler = new
		// ListHandler<ProtocolLogJdo>(
		// this.dataProvider.getList());
		// columnSortHandler.setComparator(timestampColumn,
		// new Comparator<ProtocolLogJdo>() {
		// public int compare(ProtocolLogJdo o1, ProtocolLogJdo o2) {
		// if (o1 == o2) {
		// return 0;
		// }
		// if (o1 != null) {
		// return (o2 != null) ? o1.Timestamp
		// .compareTo(o2.Timestamp) : 1;
		// }
		// return -1;
		// }
		// });
		// table.addColumnSortHandler(columnSortHandler);
		//
		// // sort order
		// this.table.getColumnSortList().push(CompleteLogUi.this.timestampColumn);
		// this.table.getColumnSortList().push(CompleteLogUi.this.timestampColumn);
	}
}
