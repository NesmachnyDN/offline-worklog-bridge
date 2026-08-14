package io.github.nesmachnydn.worklogbridge.adapter;

import io.github.nesmachnydn.worklogbridge.domain.PublishReceipt;
import io.github.nesmachnydn.worklogbridge.domain.TimesheetEntry;

public interface DestinationTimesheetPort {
    String name();
    PublishReceipt publish(TimesheetEntry entry);
}
