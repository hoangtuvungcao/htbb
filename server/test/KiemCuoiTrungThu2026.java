import java.io.*;
import java.nio.file.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
public final class KiemCuoiTrungThu2026 {
    static class Part {
        BufferedImage image;int[][][] r,o=new int[4][6][2];
        Part(String type,int id,int count)throws Exception{
            DataInputStream in=new DataInputStream(Files.newInputStream(Paths.get("map/c/"+type+"/"+id)));
            byte[] png=new byte[in.readUnsignedShort()];in.readFully(png);
            image=ImageIO.read(new ByteArrayInputStream(png));
            byte[] data=new byte[in.readUnsignedShort()];in.readFully(data);in.close();
            in=new DataInputStream(new ByteArrayInputStream(data));in.skipBytes(6);
            r=new int[4][count][4];
            for(int d=0;d<4;d++){for(int f=0;f<count;f++)for(int k=0;k<4;k++)r[d][f][k]=in.readUnsignedByte();
                for(int f=0;f<6;f++)for(int k=0;k<2;k++)o[d][f][k]=in.readByte();}
        }
        int[] visible(int dir,int frame,boolean head){
            int d=Math.min(dir,2),pose=head?0:new int[]{0,0,1,2,3,4}[frame];
            int[] a=r[d][pose];int l=999,t=999,rr=-1,b=-1;
            for(int y=0;y<a[3];y++)for(int x=0;x<a[2];x++)
                if((image.getRGB(a[0]+x,a[1]+y)>>>24)>=128){l=Math.min(l,x);rr=Math.max(rr,x);t=Math.min(t,y);b=Math.max(b,y);}
            int dx=o[d][frame][0],dy=o[d][frame][1];
            if(dir==3)return new int[]{-dx-1-rr,dy+t,-dx-1-l,dy+b};
            return new int[]{dx+l,dy+t,dx+rr,dy+b};
        }
        int[] seat(int d,int f){
            int[] a=r[d][new int[]{0,0,1,2,3,4}[f]];
            int left=999,right=-1,bottom=-1;
            for(int y=0;y<a[3];y++)for(int x=0;x<a[2];x++)
                if((image.getRGB(a[0]+x,a[1]+y)>>>24)>=128){left=Math.min(left,x);right=Math.max(right,x);bottom=Math.max(bottom,y);}
            if(right<0)throw new IllegalStateException("Khung rong");
            return new int[]{o[d][f][0]+left+(right-left+1)/2,o[d][f][1]+bottom};
        }
    }
    public static void chay(int sex,real.Char c)throws Exception{
        Part target=new Part("body",73+sex,5),body=new Part("body",83+sex,5);
        Part walk=new Part("body",81+sex,5),head=new Part("head",86+sex,1),oldHead=new Part("head",84+sex,1);
        for(int dir=0;dir<4;dir++)for(int f=0;f<6;f++){
            for(String part:new String[]{"head","body"}){
                boolean isHead=part.equals("head");
                Part original=isHead?new Part("head",76+sex,1):target;
                Part actualPart=isHead?head:body;
                int[] wanted=original.visible(dir,f,isHead),actual=actualPart.visible(dir,f,isHead);
                for(int edge=0;edge<4;edge++)
                    Kiem.dung("Vung hien thi "+part+" huong "+dir+" frame "+f+" canh "+edge,
                        Math.abs(wanted[edge]-actual[edge])<=1);
            }
        }
        Kiem.bang("Di bo dung than cu",81+sex,(int)c.getIDModel()[2]);
        c.animalRide=new data.Animal();
        Kiem.bang("Cuoi dung than rieng",83+sex,(int)c.getIDModel()[2]);
        Kiem.bang("Cuoi dung dau rieng",86+sex,(int)c.getIDModel()[0]);
        c.animalRide=null;
        Kiem.bang("Xuong thu phuc hoi than",81+sex,(int)c.getIDModel()[2]);
        Kiem.bang("Xuong thu phuc hoi dau",84+sex,(int)c.getIDModel()[0]);
    }
}
