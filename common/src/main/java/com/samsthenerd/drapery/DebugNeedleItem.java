package com.samsthenerd.drapery;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

public class DebugNeedleItem extends Item {
    public DebugNeedleItem(){
        super(new Item.Settings());
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if(context.getWorld().isClient()) {
            if (context.getWorld().getBlockEntity(context.getBlockPos()) instanceof ClothHaverDuck clothHaver) {

                ClothMesh mesh = clothHaver.getMesh();
                if(mesh != null){
                    if(context.getPlayer().isSneaking()){
                        clothHaver.setSimTicking(!clothHaver.isSimTicking());
                        DraperyMod.LOGGER.info("is ticking: " + clothHaver.isSimTicking());
                    } else {
                        mesh.stepClothSim();
                    }
                }
                return ActionResult.SUCCESS;
            }
        }
        return super.useOnBlock(context);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public Text getName() {
        return super.getName().copy().setStyle(Style.EMPTY.withColor(0xFF_f8a6bd));
    }
}
