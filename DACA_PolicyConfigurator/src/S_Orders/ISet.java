package S_Orders;

import S_Customers.IRCustomerID;

import java.sql.SQLException;

public interface ISet {
    void set(IRCustomerID cid, String country) throws SQLException;
}
