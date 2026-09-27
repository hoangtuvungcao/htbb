import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** Giu hinh Trung Thu, lay delta vi tri va kich thuoc tung khung tu Hac Lang / Than Nu. */
public final class TaoThoiTrangTrungThu2026 {
    private static final int W = 85, H = 102;
    private static final Path ROOT = Paths.get("map/trungthu2026");
    public static void main(String[] args) throws Exception {
        for (int sex = 0; sex < 2; sex++) {
            String name = sex == 0 ? "nam" : "nu";
            BufferedImage original = ImageIO.read(new File("map/icon/" + (866 + sex) + ".png"));
            BufferedImage source = ImageIO.read(ROOT.resolve(name + "-source.png").toFile());
            if (original == null || source == null) throw new IOException("Thieu anh " + name);
            BufferedImage[][] frames = palette(normalize(source, original));
            // Dau dung mot khung moi huong theo PART_OF_FRAME[2] cua client.
            pack(frames, 0, 60, 1, Paths.get("map/c/head/" + (84 + sex)));
            pack(frames, 52, 32, 5, Paths.get("map/c/body/" + (81 + sex)));
            pack(frames, 76, 26, 5, Paths.get("map/c/leg/" + (81 + sex)));
            BufferedImage sheet = new BufferedImage(W * 5, H * 3, BufferedImage.TYPE_INT_ARGB);
            for (int dir = 0; dir < 3; dir++) for (int pose = 0; pose < 5; pose++)
                for(int yy=0;yy<H;yy++) for(int xx=0;xx<W;xx++)
                    sheet.setRGB(pose*W+xx,dir*H+yy,frames[dir][pose].getRGB(xx,yy));
            ImageIO.write(sheet, "png", ROOT.resolve(name + "-frames.png").toFile());
            fitSize(sex,name); riding(sex,name); System.out.println(name + ": chuyen dong theo " + (sex==0 ? "Hac Lang" : "Than Nu"));
        }
    }
    private static Rectangle bounds(BufferedImage image) throws IOException {
        int left=image.getWidth(), top=image.getHeight(), right=-1, bottom=-1;
        for(int y=0;y<image.getHeight();y++) for(int x=0;x<image.getWidth();x++)
            if((image.getRGB(x,y) >>> 24) >= 128) {
                left=Math.min(left,x); top=Math.min(top,y);
                right=Math.max(right,x); bottom=Math.max(bottom,y);
            }
        if(right < left) throw new IOException("Khung trong");
        return new Rectangle(left,top,right-left+1,bottom-top+1);
    }
    private static BufferedImage[][] normalize(BufferedImage source, BufferedImage original) throws IOException {
        BufferedImage[][] cells = new BufferedImage[3][5];
        Rectangle[][] boxes = new Rectangle[3][5];
        Rectangle target = bounds(original.getSubimage(0,0,W,H));
        int maxWidth=0, maxHeight=0;
        for(int dir=0;dir<3;dir++) for(int pose=0;pose<5;pose++) {
            int x0=pose*source.getWidth()/5, x1=(pose+1)*source.getWidth()/5;
            int y0=dir*source.getHeight()/3, y1=(dir+1)*source.getHeight()/3;
            cells[dir][pose]=source.getSubimage(x0,y0,x1-x0,y1-y0);
            boxes[dir][pose]=bounds(cells[dir][pose]);
            maxWidth=Math.max(maxWidth,boxes[dir][pose].width);
            maxHeight=Math.max(maxHeight,boxes[dir][pose].height);
        }
        double scale=Math.min((double)target.height/maxHeight, (W-4.0)/maxWidth);
        BufferedImage[][] frames = new BufferedImage[3][5];
        for(int dir=0;dir<3;dir++) for(int pose=0;pose<5;pose++) {
            Rectangle b=boxes[dir][pose];
            int w=Math.max(1,(int)Math.round(b.width*scale));
            int h=Math.max(1,(int)Math.round(b.height*scale));
            int x=(W-w)/2, y=target.y+target.height-h;
            BufferedImage frame=new BufferedImage(W,H,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g=frame.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(cells[dir][pose],x,y,x+w,y+h,b.x,b.y,b.x+b.width,b.y+b.height,null);
            g.dispose();
            // Pixel art: alpha nhi phan, khong tao vien mo khi client thu phong.
            for(int yy=0;yy<H;yy++) for(int xx=0;xx<W;xx++) {
                int rgba=frame.getRGB(xx,yy);
                frame.setRGB(xx,yy,(rgba>>>24)<128 ? 0 : rgba | 0xff000000);
            }
            frames[dir][pose]=frame;
        }
        // Dau bat dong la rang buoc dinh dang client; ghep cung dau de khong dut co.
        for(int dir=0;dir<3;dir++) for(int pose=1;pose<5;pose++)
            for(int y=0;y<56;y++) for(int x=0;x<W;x++)
                frames[dir][pose].setRGB(x,y,frames[dir][0].getRGB(x,y));
        return frames;
    }
    private static BufferedImage[][] palette(BufferedImage[][] frames) {
        java.util.Map<Integer,Integer> histogram=new java.util.HashMap<Integer,Integer>();
        for(BufferedImage[] row:frames) for(BufferedImage frame:row)
            for(int y=0;y<H;y++) for(int x=0;x<W;x++) {
                int color=frame.getRGB(x,y);
                if((color>>>24)>0) histogram.put(color,histogram.getOrDefault(color,0)+1);
            }
        java.util.List<Integer> colors=new java.util.ArrayList<Integer>(histogram.keySet());
        java.util.List<java.util.List<Integer>> boxes=new java.util.ArrayList<java.util.List<Integer>>();
        boxes.add(colors);
        while(boxes.size()<255) {
            int chosen=-1,shift=0; long bestScore=-1;
            for(int k=0;k<boxes.size();k++) {
                java.util.List<Integer> box=boxes.get(k);
                if(box.size()<2) continue;
                int population=0;
                for(int c:box) population+=histogram.get(c);
                for(int s:new int[]{16,8,0}) {
                    int lo=255,hi=0;
                    for(int c:box){int v=(c>>s)&255;lo=Math.min(lo,v);hi=Math.max(hi,v);}
                    long score=(long)(hi-lo)*population;
                    if(score>bestScore){bestScore=score;chosen=k;shift=s;}
                }
            }
            if(chosen<0) break;
            java.util.List<Integer> box=boxes.remove(chosen);
            final int channel=shift;
            java.util.Collections.sort(box,(a,b)->{
                int diff=Integer.compare((a>>channel)&255,(b>>channel)&255);
                return diff!=0 ? diff : Integer.compare(a,b);
            });
            int total=0,sum=0,cut=1;
            for(int c:box) total+=histogram.get(c);
            for(int i=0;i<box.size()-1;i++){sum+=histogram.get(box.get(i));cut=i+1;if(sum>=total/2)break;}
            boxes.add(new java.util.ArrayList<Integer>(box.subList(0,cut)));
            boxes.add(new java.util.ArrayList<Integer>(box.subList(cut,box.size())));
        }
        colors=new java.util.ArrayList<Integer>();
        for(java.util.List<Integer> box:boxes) {
            long rr=0,gg=0,bb=0,total=0;
            for(int c:box){int n=histogram.get(c);total+=n;rr+=((c>>16)&255)*(long)n;gg+=((c>>8)&255)*(long)n;bb+=(c&255)*(long)n;}
            colors.add(0xff000000|((int)(rr/total)<<16)|((int)(gg/total)<<8)|(int)(bb/total));
        }
        int n=colors.size();
        byte[] r=new byte[n+1],g=new byte[n+1],b=new byte[n+1],a=new byte[n+1];
        for(int i=1;i<=n;i++) {
            int c=colors.get(i-1);r[i]=(byte)(c>>16);g[i]=(byte)(c>>8);b[i]=(byte)c;a[i]=(byte)255;
        }
        java.awt.image.IndexColorModel model=new java.awt.image.IndexColorModel(8,n+1,r,g,b,a);
        java.util.Map<Integer,Integer> lookup=new java.util.HashMap<Integer,Integer>();
        for(int dir=0;dir<3;dir++) for(int pose=0;pose<5;pose++) {
            BufferedImage indexed=new BufferedImage(W,H,BufferedImage.TYPE_BYTE_INDEXED,model);
            for(int y=0;y<H;y++) for(int x=0;x<W;x++) {
                int c=frames[dir][pose].getRGB(x,y),index=0;
                if((c>>>24)>0) {
                    Integer cached=lookup.get(c);
                    if(cached!=null) index=cached;
                    else {
                        int best=Integer.MAX_VALUE;
                        for(int i=1;i<=n;i++) {
                            int dr=((c>>16)&255)-(r[i]&255),dg=((c>>8)&255)-(g[i]&255),db=(c&255)-(b[i]&255);
                            int d=dr*dr+dg*dg+db*db;
                            if(d<best){best=d;index=i;}
                        }
                        lookup.put(c,index);
                    }
                }
                indexed.getRaster().setSample(x,y,0,index);
            }
            frames[dir][pose]=indexed;
        }
        return frames;
    }
    private static final class Motion {
        final int[][][] rect;
        final int[][][] offset = new int[4][6][2];
        Motion(Path path, int count) throws IOException {
            rect=new int[4][count][4];
            try(DataInputStream in=new DataInputStream(Files.newInputStream(path))) {
                int n=in.readUnsignedShort();
                byte[] png=new byte[n];in.readFully(png);
                byte[] bytes=new byte[in.readUnsignedShort()];in.readFully(bytes);
                DataInputStream d=new DataInputStream(new ByteArrayInputStream(bytes));
                d.skipBytes(6);
                for(int dir=0;dir<4;dir++) {
                    for(int pose=0;pose<count;pose++) for(int k=0;k<4;k++)
                        rect[dir][pose][k]=d.readUnsignedByte();
                    for(int f=0;f<6;f++) for(int k=0;k<2;k++) offset[dir][f][k]=d.readByte();
                }
            }
        }
    }
    private static int donor(String part, int sex) { return (part.equals("head") ? 76 : 73)+sex; }

    private static void pack(BufferedImage[][] frames,int y,int h,int count,Path out) throws Exception {
        String part=out.getParent().getFileName().toString();
        int sex=Integer.parseInt(out.getFileName().toString())-(part.equals("head")?84:81);
        Motion motion=new Motion(Paths.get("map/c/"+part+"/"+donor(part,sex)),count);
        int[][] widths=new int[3][count], heights=new int[3][count];
        int cellW=0,cellH=0;
        for(int dir=0;dir<3;dir++) for(int pose=0;pose<count;pose++) {
            widths[dir][pose]=Math.max(1,W+motion.rect[dir][pose][2]-motion.rect[dir][0][2]);
            heights[dir][pose]=Math.max(1,h+motion.rect[dir][pose][3]-motion.rect[dir][0][3]);
            cellW=Math.max(cellW,widths[dir][pose]);cellH=Math.max(cellH,heights[dir][pose]);
        }
        if(cellW*2>255 || cellH*(count-1)>255) throw new IOException("Atlas vuot toa do byte: "+out);
        BufferedImage atlas=new BufferedImage(cellW*3,cellH*count,BufferedImage.TYPE_BYTE_INDEXED,
                (java.awt.image.IndexColorModel)frames[0][0].getColorModel());
        for(int dir=0;dir<3;dir++) for(int pose=0;pose<count;pose++) {
            int w=widths[dir][pose],hh=heights[dir][pose];
            for(int yy=0;yy<hh;yy++) for(int xx=0;xx<w;xx++)
                atlas.getRaster().setSample(dir*cellW+xx,pose*cellH+yy,0,
                    frames[dir][pose].getRaster().getSample(xx*W/w,y+yy*h/hh,0));
        }
        ByteArrayOutputStream imageBytes=new ByteArrayOutputStream();
        ImageIO.write(atlas,"png",imageBytes);
        if(imageBytes.size()>32767) throw new IOException("PNG vuot short: "+out);
        ByteArrayOutputStream buffer=new ByteArrayOutputStream();
        DataOutputStream data=new DataOutputStream(buffer);
        data.writeByte(0);data.writeByte(0);data.writeByte(W);data.writeByte(h);
        data.writeByte(-W/2);data.writeByte(y-H);
        int[] poseOfFrame={0,0,1,2,3,4};
        for(int dir=0;dir<4;dir++) {
            int visualDir=Math.min(dir,2);
            for(int pose=0;pose<count;pose++) {
                data.writeByte(visualDir*cellW);data.writeByte(pose*cellH);
                data.writeByte(widths[visualDir][pose]);data.writeByte(heights[visualDir][pose]);
            }
            for(int frame=0;frame<6;frame++) {
                int pose=count==1 ? 0 : poseOfFrame[frame];
                int w=widths[visualDir][pose],hh=heights[visualDir][pose];
                // Dung chinh delta tung frame cua bo goc; bu kich thuoc quanh tam va day.
                int dx=-W/2-(w-W)/2+motion.offset[dir][frame][0]-motion.offset[dir][0][0];
                int dy=y-H-(hh-h)+motion.offset[dir][frame][1]-motion.offset[dir][0][1];
                if(dx < -128 || dx > 127 || dy < -128 || dy > 127)
                    throw new IOException("Offset vuot signed byte: "+out);
                data.writeByte(dx);data.writeByte(dy);
            }
        }
        data.flush();
        try(DataOutputStream file=new DataOutputStream(Files.newOutputStream(out))) {
            file.writeShort(imageBytes.size());file.write(imageBytes.toByteArray());
            file.writeShort(buffer.size());file.write(buffer.toByteArray());
        }
    }
    private static void fitSize(int sex,String name) throws Exception {
        preview(sex,name,false);
        preview(sex,name+"-mau",true);
        BufferedImage current=ImageIO.read(ROOT.resolve(name+"-motion.png").toFile());
        BufferedImage model=ImageIO.read(ROOT.resolve(name+"-mau-motion.png").toFile());
        int before=bounds(current.getSubimage(0,0,128,144)).height;
        int target=bounds(model.getSubimage(0,0,128,144)).height;
        double scale=(double)target/before;
        for(String part:new String[]{"head","body","leg"})
            shrink(Paths.get("map/c/"+part+"/"+((part.equals("head")?84:81)+sex)),
                part.equals("head")?1:5,scale);
        Files.write(ROOT.resolve(name+"-scale.txt"),Double.toString(scale).getBytes("UTF-8"));
        System.out.println(name+": cao "+before+" -> muc tieu "+target+" px; scale="+scale);
        preview(sex,name,false);
    }
    private static void shrink(Path path,int count,double scale) throws Exception {
        byte[] metadata;
        BufferedImage old;
        try(DataInputStream in=new DataInputStream(Files.newInputStream(path))) {
            byte[] png=new byte[in.readUnsignedShort()];in.readFully(png);
            old=ImageIO.read(new ByteArrayInputStream(png));
            metadata=new byte[in.readUnsignedShort()];in.readFully(metadata);
        }
        int nw=(int)Math.round(old.getWidth()*scale),nh=(int)Math.round(old.getHeight()*scale);
        BufferedImage small=new BufferedImage(nw,nh,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<nh;y++)for(int x=0;x<nw;x++)
            small.setRGB(x,y,old.getRGB(Math.min(old.getWidth()-1,(int)(x/scale)),
                Math.min(old.getHeight()-1,(int)(y/scale))));
        // Tat ca toa do/khung va diem neo cung mot ti le, khong chi thu PNG.
        int pos=6;
        for(int dir=0;dir<4;dir++) {
            for(int frame=0;frame<count;frame++) {
                int x=metadata[pos]&255,y=metadata[pos+1]&255,w=metadata[pos+2]&255,h=metadata[pos+3]&255;
                int xx=(int)Math.round(x*scale),yy=(int)Math.round(y*scale);
                metadata[pos++]=(byte)xx;metadata[pos++]=(byte)yy;
                metadata[pos++]=(byte)((int)Math.round((x+w)*scale)-xx);
                metadata[pos++]=(byte)((int)Math.round((y+h)*scale)-yy);
            }
            for(int i=0;i<12;i++,pos++)metadata[pos]=(byte)Math.round(metadata[pos]*scale);
        }
        for(int i=0;i<4;i++)metadata[i]=(byte)Math.round((metadata[i]&255)*scale);
        for(int i=4;i<6;i++)metadata[i]=(byte)Math.round(metadata[i]*scale);
        ByteArrayOutputStream png=new ByteArrayOutputStream();ImageIO.write(small,"png",png);
        try(DataOutputStream out=new DataOutputStream(Files.newOutputStream(path))) {
            out.writeShort(png.size());out.write(png.toByteArray());
            out.writeShort(metadata.length);out.write(metadata);
        }
    }
    private static BufferedImage partImage(Path path) throws IOException {
        try(DataInputStream in=new DataInputStream(Files.newInputStream(path))) {
            byte[] png=new byte[in.readUnsignedShort()];in.readFully(png);
            return ImageIO.read(new ByteArrayInputStream(png));
        }
    }
    private static int[] seat(Motion m,BufferedImage img,int dir,int frame) throws IOException {
        int pose=new int[]{0,0,1,2,3,4}[frame];
        int[] r=m.rect[dir][pose];
        Rectangle box=bounds(img.getSubimage(r[0],r[1],r[2],r[3]));
        return new int[]{m.offset[dir][frame][0]+box.x+box.width/2,
            m.offset[dir][frame][1]+box.y+box.height-1};
    }
    private static void riding(int sex,String name) throws Exception {
        for(String part:new String[]{"head","body"}) {
            int id=(part.equals("head")?84:81)+sex,count=part.equals("head")?1:5;
            Path source=Paths.get("map/c/"+part+"/"+id),reference=Paths.get("map/c/"+part+"/"+donor(part,sex));
            Motion src=new Motion(source,count),ref=new Motion(reference,count);
            BufferedImage srcImg=partImage(source),refImg=partImage(reference);
            Rectangle[][] sb=new Rectangle[3][count],rb=new Rectangle[3][count];
            int cw=0,ch=0;
            for(int d=0;d<3;d++)for(int f=0;f<count;f++){
                int[] a=src.rect[d][f],b=ref.rect[d][f];
                sb[d][f]=bounds(srcImg.getSubimage(a[0],a[1],a[2],a[3]));
                rb[d][f]=bounds(refImg.getSubimage(b[0],b[1],b[2],b[3]));
                cw=Math.max(cw,rb[d][f].width);ch=Math.max(ch,rb[d][f].height);
            }
            BufferedImage atlas=new BufferedImage(cw*3,ch*count,BufferedImage.TYPE_INT_ARGB);
            for(int d=0;d<3;d++)for(int f=0;f<count;f++){
                Rectangle a=sb[d][f],b=rb[d][f];int[] r=src.rect[d][f];
                for(int yy=0;yy<b.height;yy++)for(int xx=0;xx<b.width;xx++){
                    int sx=b.width==1?0:(int)Math.round(xx*(a.width-1.0)/(b.width-1));
                    int sy=b.height==1?0:(int)Math.round(yy*(a.height-1.0)/(b.height-1));
                    atlas.setRGB(d*cw+xx,f*ch+yy,srcImg.getRGB(r[0]+a.x+sx,r[1]+a.y+sy));
                }
            }
            ByteArrayOutputStream pb=new ByteArrayOutputStream();ImageIO.write(atlas,"png",pb);
            ByteArrayOutputStream mb=new ByteArrayOutputStream();DataOutputStream m=new DataOutputStream(mb);
            m.write(new byte[]{0,0,(byte)rb[0][0].width,(byte)rb[0][0].height,0,0});
            int[] poses={0,0,1,2,3,4};
            for(int d=0;d<4;d++){
                int v=Math.min(d,2);
                for(int f=0;f<count;f++){m.writeByte(v*cw);m.writeByte(f*ch);m.writeByte(rb[v][f].width);m.writeByte(rb[v][f].height);}
                for(int f=0;f<6;f++){
                    Rectangle b=rb[v][count==1?0:poses[f]];
                    m.writeByte(ref.offset[v][f][0]+b.x);m.writeByte(ref.offset[v][f][1]+b.y);
                }
            }
            try(DataOutputStream out=new DataOutputStream(Files.newOutputStream(Paths.get("map/c/"+part+"/"+(id+2))))){
                out.writeShort(pb.size());out.write(pb.toByteArray());out.writeShort(mb.size());out.write(mb.toByteArray());
            }
        }
        preview(sex,name+"-cuoi",false);
        preview(sex,name+"-mau-cuoi",true);
    }
    private static void preview(int sex,String name,boolean original) throws Exception {
        BufferedImage sheet=new BufferedImage(128*6,144*4,BufferedImage.TYPE_INT_ARGB);
        int[] poseOfFrame={0,0,1,2,3,4};
        for(String part:new String[]{"leg","body","head"}) {
            int id=original ? donor(part,sex) : (part.equals("head")?84:81)+sex,count=part.equals("head")?1:5;
            if(name.endsWith("-cuoi")) { if(part.equals("leg"))continue; if(!original)id+=2; } Path path=Paths.get("map/c/"+part+"/"+id);
            Motion motion=new Motion(path,count);
            BufferedImage atlas;
            try(DataInputStream in=new DataInputStream(Files.newInputStream(path))) {
                byte[] bytes=new byte[in.readUnsignedShort()];in.readFully(bytes);
                atlas=ImageIO.read(new ByteArrayInputStream(bytes));
            }
            for(int dir=0;dir<4;dir++) for(int frame=0;frame<6;frame++) {
                int visualDir=Math.min(dir,2),pose=count==1?0:poseOfFrame[frame];
                int[] rect=motion.rect[visualDir][pose];
                int dx=motion.offset[visualDir][frame][0],dy=motion.offset[visualDir][frame][1];
                if(dir==3) dx=-dx-rect[2];
                for(int yy=0;yy<rect[3];yy++) for(int xx=0;xx<rect[2];xx++) {
                    int pixel=atlas.getRGB(rect[0]+(dir==3?rect[2]-1-xx:xx),rect[1]+yy);
                    int x=64+dx+xx,y=104+dy+yy;
                    if((pixel>>>24)>0)
                        { if(x<0 || x>=128 || y<0 || y>=144)throw new IOException("Preview cat pixel"); sheet.setRGB(frame*128+x,dir*144+y,pixel); }
                }
            }
        }
        ImageIO.write(sheet,"png",ROOT.resolve(name+"-motion.png").toFile());
    }
}
