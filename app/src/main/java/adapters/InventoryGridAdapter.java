package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.tk4218.inventorymanager.R;
import com.tk4218.model.ImageManager;
import com.tk4218.model.TableObject;


public class InventoryGridAdapter extends BaseAdapter {
    Context context;
    TableObject inventory;
    ImageManager imageManager;

    public InventoryGridAdapter(Context context, TableObject inventory){
        this.context = context;
        this.inventory = inventory;
        imageManager = new ImageManager();
    }


    @Override
    public int getCount() {
        return inventory.getRowCount();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.gridview_inventory_manager, null);
        }

        inventory.moveToPosition(position);
        ImageView inventoryImage = (ImageView) convertView.findViewById(R.id.image_inventory);
        if(!inventory.getString("InventoryPicture").equals("")){
            imageManager.setPic(inventory.getString("InventoryPicture"), inventoryImage, 10);
        }

        int inventoryKey = inventory.getInt("InventoryKey");
        Button picture = (Button) convertView.findViewById(R.id.button_picture);
        picture.setContentDescription(inventoryKey+"");
        Button sold = (Button) convertView.findViewById(R.id.button_sold);
        sold.setContentDescription(inventoryKey+"");
        Button info = (Button) convertView.findViewById(R.id.button_info);
        info.setContentDescription(inventoryKey+"");

        return convertView;
    }
}
