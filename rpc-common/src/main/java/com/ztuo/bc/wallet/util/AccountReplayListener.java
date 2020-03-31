package com.ztuo.bc.wallet.util;

import com.ztuo.bc.wallet.model.AddressEth;

public interface AccountReplayListener {

    void replay(AddressEth account);
}
