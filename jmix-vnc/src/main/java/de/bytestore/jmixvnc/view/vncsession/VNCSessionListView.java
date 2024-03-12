package de.bytestore.jmixvnc.view.vncsession;

import de.bytestore.jmixvnc.entity.VNCSession;

import io.jmix.flowui.view.DefaultMainViewParent;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "VNCSessions", layout = DefaultMainViewParent.class)
@ViewController("novnv_VNCSession.list")
@ViewDescriptor("vnc-session-list-view.xml")
@LookupComponent("VNCSessionsDataGrid")
@DialogMode(width = "64em")
public class VNCSessionListView extends StandardListView<VNCSession> {
}