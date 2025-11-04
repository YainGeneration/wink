// // src/AppRoutes.tsx
// import { Routes, Route, Navigate } from "react-router-dom";
// import MyPage from "./pages/MyPage";
// import MainPage from "./pages/MainPage";
// import LoginPage from "./pages/LoginPage";
// import WinkSplash from "./components/WinkSplash";
// import MyPosts from "./pages/mypage/activity/MyPosts";
// import MyLikes from "./pages/mypage/activity/MyLikes";
// import MyScrap from "./pages/mypage/activity/MyScrap";
// import AccountInfo from "./pages/mypage/settings/AccountInfo";
// import ChangePassword from "./pages/mypage/settings/ChangePassword";
// import Shipping from "./pages/mypage/settings/Shipping";
// // 등등 필요한 하위 페이지 import

// export default function AppRoutes() {
//   return (
//     <Routes>
//       {/* 메인 */}
//       <Route path="/" element={<MainPage />} />

//       {/* 로그인 */}
//       <Route path="/login" element={<LoginPage />} />

//       {/* 마이페이지 */}
//       <Route path="/mypage" element={<MyPage />}>
//         {/* MyPage 내부의 <Outlet />에 렌더링될 하위 라우트들 */}
//         <Route path="activity/posts" element={<MyPosts />} />
//         <Route path="activity/likes" element={<MyLikes />} />
//         <Route path="activity/scrap" element={<MyScrap />} />
//         <Route path="settings/accountInfo" element={<AccountInfo />} />
//         <Route path="settings/changePassword" element={<ChangePassword />} />
//         <Route path="settings/shippings" element={<Shipping />} />
//       </Route>

//       {/* 잘못된 경로 → 메인으로 리다이렉트 */}
//       <Route path="*" element={<Navigate to="/" replace />} />
//     </Routes>
//   );
// }
