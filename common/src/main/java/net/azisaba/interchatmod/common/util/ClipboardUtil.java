package net.azisaba.interchatmod.common.util;

import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class ClipboardUtil {
    public static byte @Nullable [] getImageFromClipboard() {
        Transferable content = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (content == null) return null;
        if (!content.isDataFlavorSupported(DataFlavor.imageFlavor)) return null;
        try {
            BufferedImage image = (BufferedImage) content.getTransferData(DataFlavor.imageFlavor);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);
            return os.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}
