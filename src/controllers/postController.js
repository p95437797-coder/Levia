'use strict';
const asyncHandler = require('../utils/asyncHandler');
const v = require('../validators/postValidators');
const service = require('../services/postService');
const ok = (res, data, status = 200) => res.status(status).json({ success: true, data });
exports.feed = asyncHandler(async (req, res) => ok(res, await service.listPosts(req.user.id, v.pageQuery.parse(req.query))));
exports.search = asyncHandler(async (req, res) => ok(res, await service.listPosts(req.user.id, v.searchQuery.parse(req.query))));
exports.byUser = asyncHandler(async (req, res) => {
  const { username } = v.usernameParams.parse(req.params);
  ok(res, await service.listPosts(req.user.id, { ...v.pageQuery.parse(req.query), username }));
});
exports.create = asyncHandler(async (req, res) => ok(res, await service.createPost(req.user.id, v.createPost.parse(req.body)), 201));
exports.like = asyncHandler(async (req, res) => { const { postId } = v.postIdParams.parse(req.params); ok(res, await service.likePost(req.user.id, postId)); });
exports.unlike = asyncHandler(async (req, res) => { const { postId } = v.postIdParams.parse(req.params); ok(res, await service.unlikePost(req.user.id, postId)); });
exports.comments = asyncHandler(async (req, res) => { const { postId } = v.postIdParams.parse(req.params); ok(res, await service.listComments(req.user.id, postId, 50)); });
exports.addComment = asyncHandler(async (req, res) => { const { postId } = v.postIdParams.parse(req.params); const { body } = v.commentBody.parse(req.body); ok(res, await service.addComment(req.user.id, postId, body), 201); });
