package adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tk4218.inventorymanager.R;
import com.tk4218.model.DataSource;
import com.tk4218.model.ImageManager;
import com.tk4218.model.TableObject;

import java.util.List;

/**
 * Created by Tk4218 on 6/18/2016.
 */
public class InventoryListAdapter extends BaseExpandableListAdapter{

    private Context context;
    private List<String> groupData;
    private TableObject inventoryData;
    private ImageManager imageManager;
    DataSource mDbc;


    /*****************************************************************
     *  Constructor
     *****************************************************************/
    public InventoryListAdapter(Context context, List<String> groupData, TableObject listData) {
        this.context = context;
        this.groupData = groupData;
        inventoryData = listData;
        imageManager = new ImageManager();
        mDbc = new DataSource(context);
        mDbc.open();
    }

    /******************************************************************
     * Child Methods
     ******************************************************************/
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        TableObject child = inventoryData.filter("Style", groupData.get(groupPosition));
        child.moveToPosition(childPosition);
        return child.getString("Style");
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
       //TODO: Implement List Item View

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_inventory_item, null);
        }

        TextView inventorySize = (TextView) convertView.findViewById(R.id.item_size);
        TextView inventoryItem = (TextView) convertView.findViewById(R.id.inventory_list_item);
        TextView inventoryCount = (TextView) convertView.findViewById(R.id.item_count);
        TableObject child = inventoryData.filter("Style", groupData.get(groupPosition));
        child.moveToPosition(childPosition);
        inventoryCount.setText(child.getInt("StyleCount")+"");
        inventorySize.setText(child.getString("SizeShort"));
        inventoryItem.setText(child.getString("Style"));
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return inventoryData.filter("Style", groupData.get(groupPosition)).getRowCount();
    }

    /*********************************************************************
     * Group Methods
     **********************************************************************/
    @Override
    public Object getGroup(int groupPosition) {
        return groupData.get(groupPosition);
    }
    @Override
    public int getGroupCount() {
        return groupData.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_inventory_group, null);
        }

        TextView inventoryGroup = (TextView) convertView.findViewById(R.id.inventory_group_header);
        TextView styleCount = (TextView) convertView.findViewById(R.id.styleCount);
        ImageView styleImage = (ImageView) convertView.findViewById(R.id.styleImage);

        inventoryGroup.setText(groupData.get(groupPosition));
        styleCount.setText(inventoryData.filter("Style", groupData.get(groupPosition)).sum("StyleCount")+"");
        TableObject stylePicture  = mDbc.getStyleImage(groupData.get(groupPosition));

        int width = styleImage.getLayoutParams().width;
        int height = styleImage.getLayoutParams().height;
        imageManager.loadBitmap(stylePicture.getString("StylePicture"), styleImage, width, height, context.getResources());
        //styleImage.setImageBitmap(imageManager.setPic(stylePicture.getString("StylePicture"), styleImage.getWidth(), styleImage.getHeight()));
        //imageManager.setPic(stylePicture.getString("StylePicture"), styleImage, 25);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}



