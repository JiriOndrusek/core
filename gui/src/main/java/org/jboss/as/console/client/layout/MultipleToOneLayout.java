package org.jboss.as.console.client.layout;

import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.as.console.client.widgets.ContentDescription;
import org.jboss.ballroom.client.widgets.ContentGroupLabel;
import org.jboss.ballroom.client.widgets.ContentHeaderLabel;
import org.jboss.ballroom.client.widgets.tables.DefaultPager;
import org.jboss.ballroom.client.widgets.tabs.FakeTabPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Heiko Braun
 * @date 11/28/11
 */
public class MultipleToOneLayout {

    private LayoutPanel layout = null;

    private String title = "TITLE";
    private String headline = "HEADLINE";
    private SafeHtml description = null;

    private Widget toolStrip = null;

    private NamedTable master;
    private Widget masterTools;

    private NamedWidget detail;
    private List<NamedWidget> details = new ArrayList<NamedWidget>();
    private Widget detailTools;
    private boolean isPlain;

    private Widget headlineWidget = null;
    private Widget masterFooter = null;

    public MultipleToOneLayout setHeadlineWidget(Widget widget)
    {
        this.headlineWidget = widget;
        return this;
    }

    public MultipleToOneLayout setTitle(String title)
    {
        this.title = title;
        return this;
    }

    @Deprecated
    public MultipleToOneLayout setTopLevelTools(Widget toolstrip)
    {
        this.toolStrip = toolstrip;
        return this;
    }

    public MultipleToOneLayout setMasterTools(Widget toolstrip)
    {
        this.masterTools = toolstrip;
        return this;
    }

    @Deprecated
    public MultipleToOneLayout setDescription(String description) {
        this.description = new SafeHtmlBuilder().appendHtmlConstant(description).toSafeHtml();
        return this;
    }

    public MultipleToOneLayout setDescription(SafeHtml description) {
        this.description = description;
        return this;
    }

    public MultipleToOneLayout setHeadline(String headline) {
        this.headline = headline;
        return this;
    }


    public MultipleToOneLayout setMaster(String title, CellTable table)
    {
        this.master = new NamedTable(title, table);
        return this;
    }

    public MultipleToOneLayout setDetail(String title, Widget detail)
    {
        if(!details.isEmpty())
            throw new IllegalStateException("Can either have single OR multiple details, but not both");
        this.detail = new NamedWidget(title, detail);

        return this;
    }

    public MultipleToOneLayout addDetail(String title, Widget detail)
    {
        if(this.detail!=null)
            throw new IllegalStateException("Can either have single OR multiple details, but not both");
        details.add(new NamedWidget(title, detail));
        return this;
    }

    public Widget build() {

        if(null==master)
            throw new IllegalStateException("no master set");

        layout  = new LayoutPanel();
        layout.getElement().setId("LayoutPanel");
        layout.setStyleName("fill-layout");

        FakeTabPanel titleBar = null;
        if(!isPlain) {
            titleBar = new FakeTabPanel(title);
            layout.add(titleBar);
        }

        if(this.toolStrip !=null)
        {
            layout.add(toolStrip);
        }

        VerticalPanel panel = new VerticalPanel();
        panel.setStyleName("rhs-content-panel");

        ScrollPanel scroll = new ScrollPanel(panel);
        scroll.getElement().setId("ScrollPanel");
        layout.add(scroll);

        int offset = isPlain ? 0 : 40;

        if(toolStrip!=null)
        {
            if(!isPlain)layout.setWidgetTopHeight(titleBar, 0, Style.Unit.PX, 40, Style.Unit.PX);
            layout.setWidgetTopHeight(toolStrip, offset, Style.Unit.PX, 30, Style.Unit.PX);
            layout.setWidgetTopHeight(scroll, offset+30, Style.Unit.PX, 100, Style.Unit.PCT);
        }
        else
        {
            if(!isPlain)layout.setWidgetTopHeight(titleBar, 0, Style.Unit.PX, 40, Style.Unit.PX);
            layout.setWidgetTopHeight(scroll, offset, Style.Unit.PX, 100, Style.Unit.PCT);
        }

        if(null==headlineWidget)
        {
            panel.add(new ContentHeaderLabel(headline));
        }
        else
        {
            panel.add(headlineWidget);
        }

        if(null==description)
            panel.add(new ContentDescription("DESCRIPTION"));
        else
            panel.add(new ContentDescription(description.asString()));

        if(master!=null)
        {
            if(master.title!=null && !master.title.isEmpty())
                panel.add(new ContentGroupLabel(master.title));

            if(masterTools!=null) panel.add(masterTools);

            master.widget.getElement().setAttribute("role", "application");

            panel.add(master.widget);

            DefaultPager pager = new DefaultPager();
            pager.setDisplay(master.widget);
            panel.add(pager);

            if(masterFooter!=null)
            {
                masterFooter.addStyleName("table-footer");
                panel.add(masterFooter);
            }
        }

        // -----

        if(detail!=null)
        {
            detail.widget.getElement().setId("master_detail-detail");
            if(detail.title!=null && !detail.title.isEmpty())
                panel.add(new ContentGroupLabel(detail.title));

            if(detailTools!=null) panel.add(detailTools);
            panel.add(detail.widget);
        }
        else if(details.size()>0)
        {
            TabPanel tabs = new TabPanel();
            tabs.getElement().setId("master_detail-detail");
            tabs.setStyleName("default-tabpanel");
            tabs.getElement().setAttribute("style", "margin-top:15px;");

            for(NamedWidget item : details)
            {

                item.widget.getElement().setAttribute("role", "application");
                tabs.add(item.widget, item.title);
            }

            panel.add(tabs);

            if(!details.isEmpty())
                tabs.selectTab(0);

        }

        //layout.getElement().setAttribute("style", "border:1px solid red");
        return layout;
    }

    public MultipleToOneLayout setDetailTools(Widget widget) {
        this.detailTools = widget;
        return this;
    }

    public MultipleToOneLayout setPlain(boolean b) {
        this.isPlain = b;
        return this;
    }

    public MultipleToOneLayout setMasterFooter(Widget widget) {
        this.masterFooter = widget;
        return this;
    }

    public interface ValueCallback<T> {
        T getValue();
    }
}
