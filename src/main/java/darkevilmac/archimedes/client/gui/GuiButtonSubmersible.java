package darkevilmac.archimedes.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiButtonSubmersible extends GuiButton {

    public boolean submerse = false;

    public GuiButtonSubmersible(int buttonId, int x, int y) {
        super(buttonId, x, y, 32, 32, "");
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            mc.getTextureManager().bindTexture(new ResourceLocation("archimedesshipsplus", "textures/gui/submerse.png"));
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            boolean mouseOver = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            int yOffset = 0;

            if (mouseOver) {
                yOffset += 32;
            }

            int xOffset = 0;

            if (!submerse) {
                xOffset += 32;
            }

            this.drawTexturedModalRect(this.xPosition, this.yPosition, xOffset, yOffset, this.width, this.height);

            if (mouseOver) {
                int stringWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth((submerse ? "Submerse Ship" : "Don't Submerse Ship"));
                drawHoveringText((submerse ? "Submerse Ship" : "Don't Submerse Ship"),
                        mouseX + (stringWidth / 2) + 32, mouseY - 12, Minecraft.getMinecraft().fontRenderer);
            }
        }
    }

    protected void drawHoveringText(String text, int x, int y, FontRenderer font) {
        int k1 = 6839882;

        int k = (this.width - this.width) / 2;
        int l = (this.height - this.height) / 2;

        for (int i1 = 0; i1 < 3; ++i1) {

            int l1 = x - (k + 60);
            int i2 = y - (l + 14 + 19 * i1);

            if (l1 >= 0 && i2 >= 0 && l1 < 108 && i2 < 19) {
                this.drawTexturedModalRect(k + 60, l + 14 + 19 * i1, 0, 204, 108, 19);
                k1 = 16777088;
            } else {
                this.drawTexturedModalRect(k + 60, l + 14 + 19 * i1, 0, 166, 108, 19);
            }

            font.drawSplitString(text, k + 62, l + 16 + 19 * i1, 104, k1);
            font = Minecraft.getMinecraft().fontRenderer;
            k1 = 8453920;
            font.drawStringWithShadow(text, k + 62 + 104 - font.getStringWidth(text), l + 16 + 19 * i1 + 7, k1);
        }
    }
}