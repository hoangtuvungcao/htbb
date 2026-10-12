//using System;
//using System.Collections;
//using System.Collections.Generic;
//using System.Net.Http;
//using System.Text.RegularExpressions;
//using System.Threading.Tasks;
//using UnityEngine;
//using UnityEngine.Networking;
//using UnityEngine.SceneManagement;

//public class UnityRequest : MonoBehaviour
//{
//    private async void Start()
//    {
//        await Main();
//    }
//    static async Task Main()
//    {
//        using (HttpClient client = new HttpClient())
//        {
//            // URL của trang web bạn muốn lấy dữ liệu
//            string url = "http://serverkpah.free.nf/listServer.txt";

//            // Gửi yêu cầu GET tới URL
//            HttpResponseMessage response = await client.GetAsync(url);

//            // Đảm bảo rằng yêu cầu thành công
//            response.EnsureSuccessStatusCode();

//            // Đọc dữ liệu từ phản hồi
//            string responseBody = await response.Content.ReadAsStringAsync();
//            string pattern = @"^([^:]+):([^:]+):([^:]+):[^:]+$";
//            Match match = Regex.Match(responseBody, pattern);

//            if (match.Success)
//            {
//                string name = match.Groups[1].Value;
//                string host = match.Groups[2].Value;
//                string port = match.Groups[3].Value;
//                ServerListScr.IP = host;
//                ServerListScr_HAIRMOD_PROTECTED.IP = host;
//                ServerListScr_HAIRMOD_PROTECTED.portWeb = short.Parse(port);
//                ServerListScr.portWeb = short.Parse(port);
//                SceneManager.LoadScene("test");
//            }

//        }
//    }
//}
