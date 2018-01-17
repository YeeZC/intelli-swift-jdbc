package com.fr.swift.fine.adaptor.update;

import com.finebi.base.constant.FineEngineType;
import com.finebi.conf.internalimp.connection.FineConnectionImp;
import com.finebi.conf.internalimp.table.FineDBBusinessTable;
import com.finebi.conf.internalimp.table.FineSQLBusinessTable;
import com.finebi.conf.structure.bean.connection.FineConnection;
import com.finebi.conf.structure.bean.table.FineBusinessTable;
import com.finebi.conf.utils.FineConnectionUtils;
import com.fr.swift.adaptor.update.SwiftUpdateManager;
import com.fr.swift.manager.ProviderManager;
import com.fr.swift.provider.ConnectionProvider;
import com.fr.swift.resource.ResourceUtils;
import com.fr.swift.source.DataSource;
import com.fr.swift.source.db.ConnectionManager;
import com.fr.swift.source.db.IConnectionProvider;
import com.fr.swift.source.manager.IndexStuffProvider;
import junit.framework.TestCase;

/**
 * This class created on 2018-1-12 16:24:46
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI Analysis 1.0
 */
public class UpdateTableTest extends TestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        IConnectionProvider connectionProvider = new ConnectionProvider();
        ConnectionManager.getInstance().registerProvider(connectionProvider);
        String path = ResourceUtils.getFileAbsolutePath("com/fr/swift/resource/h2");
        FineConnection connection = new FineConnectionImp("jdbc:h2://" + path + "/test", "sa", "", "org.h2.Driver", "local", null, null, null);
        FineConnectionUtils.removeAllConnections();
        FineConnectionUtils.addNewConnection(connection);
    }

    public void testDBTableUpdate() throws Exception {
        FineBusinessTable fineBusinessTable = new FineDBBusinessTable("DEMO_CAPITAL_RETURN1", FineEngineType.Cube, "local", "DEMO_CAPITAL_RETURN");
        SwiftUpdateManager manager = new SwiftUpdateManager();
        manager.saveUpdateSetting(null, fineBusinessTable);

        IndexStuffProvider provider = ProviderManager.getManager().poll();
        assertTrue(true);
        assertTrue(provider.getAllTables().size() == 1);
        try {
            for (DataSource dataSource : provider.getAllTables()) {
                assertNotSame(dataSource.getMetadata().getColumnCount(), 0);
            }
        } catch (Exception e) {
            assertTrue(false);
        }

    }

    public void testSqlTableUpdate() throws Exception {
        FineBusinessTable fineBusinessTable = new FineSQLBusinessTable("tableName", "local", FineEngineType.Cube, "select * from DEMO_CAPITAL_RETURN");
        SwiftUpdateManager manager = new SwiftUpdateManager();
        manager.saveUpdateSetting(null, fineBusinessTable);

        IndexStuffProvider provider = ProviderManager.getManager().poll();
        assertTrue(true);
        assertTrue(provider.getAllTables().size() == 1);
        try {
            for (DataSource dataSource : provider.getAllTables()) {
                assertNotSame(dataSource.getMetadata().getColumnCount(), 0);
            }
        } catch (Exception e) {
            assertTrue(false);
        }
    }
}
